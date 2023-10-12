package it.eng.idsa.dataapp.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarDeserializer;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarSerializer;
import it.eng.idsa.dataapp.web.rest.exceptions.InternalRecipientException;

public class HttpHeadersUtil {

	private static final Logger logger = LoggerFactory.getLogger(HttpHeadersUtil.class);

	@SuppressWarnings("unchecked")
	public static HttpHeaders messageToHttpHeaders(Message message) {
		logger.debug("Converting message to http-header");
		
		HttpHeaders headers = new HttpHeaders();
		ObjectMapper mapper = new ObjectMapper();
		// exclude null values from map
		mapper.setSerializationInclusion(Include.NON_NULL);

		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(XMLGregorianCalendar.class, new XMLGregorianCalendarSerializer());
		mapper.registerModule(simpleModule);

		Map<String, Object> messageAsMap = mapper.convertValue(message, new TypeReference<Map<String, Object>>() {
		});

		messageAsMap.entrySet().forEach(entry -> {
			if (entry.getKey().equals("@id")) {
				headers.add("IDS-Id", message.getId().toString());
			} else if (entry.getKey().equals("@type")) {
				// when using Java it looks like this
				// headers.put("IDS-Messagetype", "ids:" +
				// message.getClass().getInterfaces()[1].getSimpleName());
				// for now we agreed to use the following, because of simplicity
				headers.add("IDS-Messagetype", entry.getValue().toString());
			} else if (entry.getKey().equals("ids:securityToken")) {
				headers.add("IDS-SecurityToken-Type", ((Map<String, Object>) entry.getValue()).get("@type").toString());
				headers.add("IDS-SecurityToken-Id", message.getSecurityToken().getId().toString());
				headers.add("IDS-SecurityToken-TokenFormat", message.getSecurityToken().getTokenFormat().toString());
				headers.add("IDS-SecurityToken-TokenValue", message.getSecurityToken().getTokenValue());
			} else if (entry.getValue() instanceof Map) {
				Map<String, Object> valueMap = (Map<String, Object>) entry.getValue();
				if (valueMap.get("@id") != null) {
					headers.add(entry.getKey().replaceFirst("ids:", "IDS-").replaceFirst(entry.getKey().substring(4, 5),
							entry.getKey().substring(4, 5).toUpperCase()), valueMap.get("@id").toString());
				} else if (valueMap.get("@value") != null) {
					headers.add(entry.getKey().replaceFirst("ids:", "IDS-").replaceFirst(entry.getKey().substring(4, 5),
							entry.getKey().substring(4, 5).toUpperCase()), valueMap.get("@value").toString());
				}
			} else if (entry.getValue() instanceof ArrayList) {
				ArrayList<String> valueList = (ArrayList<String>) entry.getValue();
				if (valueList.isEmpty()) {
					headers.add(entry.getKey().replaceFirst("ids:", "IDS-").replaceFirst(entry.getKey().substring(4, 5),
							entry.getKey().substring(4, 5).toUpperCase()), "");
				} else {
					String result = String.join(", ", valueList);
					headers.add(entry.getKey().replaceFirst("ids:", "IDS-").replaceFirst(entry.getKey().substring(4, 5),
							entry.getKey().substring(4, 5).toUpperCase()), result);
				}
			} else {
				headers.add(entry.getKey().replaceFirst("ids:", "IDS-").replaceFirst(entry.getKey().substring(4, 5),
						entry.getKey().substring(4, 5).toUpperCase()), entry.getValue().toString());
			}
		});
		logger.debug("Message successfully converted into headers");

		return headers;
	}

	public static Message httpHeadersToMessage(HttpHeaders httpHeaders) {
		Map<String, Object> messageAsHeader = new HashMap<>();

		ObjectMapper mapper = new ObjectMapper();
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addDeserializer(XMLGregorianCalendar.class, new XMLGregorianCalendarDeserializer());
		mapper.registerModule(simpleModule);

		// exclude null values from map
		mapper.setSerializationInclusion(Include.NON_NULL);

		Map<String, Object> tokeAsMap = null;
		if (httpHeaders.containsKey("ids-securitytoken-type")) {
			tokeAsMap = processDAPSTokenHttpHeaders(httpHeaders);
		}
		String type = (String) httpHeaders.getFirst("ids-messagetype");
		String id = (String) httpHeaders.getFirst("ids-id");

		List<String> fieldNames = getIDSMessageFieldNames(type);

		for (String field : fieldNames) {
			if (httpHeaders.containsKey(field.replace("ids:", "ids-"))) {
				Object value = httpHeaders.get(field.replace("ids:", "ids-")).size() == 1
						? httpHeaders.getFirst(field.replace("ids:", "ids-"))
						: httpHeaders.get(field.replace("ids:", "ids-"));

				messageAsHeader.put(field, value);
			}
		}
		messageAsHeader.put("ids:securityToken", tokeAsMap);
		messageAsHeader.put("@type", type);
		messageAsHeader.put("@id", id);

		// handle recipientConnector - List
		// handle recipientConnector - List
		List<String> recipientConnector = new ArrayList<>();
		if (httpHeaders.containsKey("ids-recipientconnector")) {
			List<String> ss = httpHeaders.get("ids-recipientconnector");
			if (!ss.isEmpty()) {
				recipientConnector.addAll(httpHeaders.get("ids-recipientconnector"));
			}
			messageAsHeader.put("ids:recipientConnector", recipientConnector);
			httpHeaders.remove("ids-recipientconnector");
		}
		// handle recipientAgent - List
		List<String> recipientAgent = new ArrayList<>();
		if (httpHeaders.containsKey("ids-recipientagent")) {
			List<String> ss = httpHeaders.get("ids-recipientagent");
			if (!ss.isEmpty()) {
				recipientAgent.addAll(httpHeaders.get("ids-recipientagent"));
			}
			messageAsHeader.put("ids:recipientAgent", recipientAgent);
			httpHeaders.remove("ids-recipientagent");
		}

		messageAsHeader.put("ids:recipientConnector", recipientConnector);
		messageAsHeader.put("ids:recipientAgent", recipientAgent);

		Message message = mapper.convertValue(messageAsHeader, Message.class);

		logger.debug("Headers successfully converted to IDS message");

		return message;
	}

	private static List<String> getIDSMessageFieldNames(String clazzName) {
		Class<?> clazz = null;
		try {
			clazz = ClassLoader.getSystemClassLoader()
					.loadClass("de.fraunhofer.iais.eis." + clazzName.replace("ids:", ""));
		} catch (ClassNotFoundException e) {
			logger.error("Error while getting IDS Message field names", e);

			throw new InternalRecipientException("Error while getting IDS Message field names");
		}
		List<String> annotatedMethods = new ArrayList<>();
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			JsonProperty annotation = method.getAnnotation(JsonProperty.class);
			if (annotation != null) {
				annotatedMethods.add(annotation.value());
			}
		}

		return annotatedMethods;
	}

	private static Map<String, Object> processDAPSTokenHttpHeaders(HttpHeaders httpHeaders) {
		Map<String, Object> tokenAsMap = new HashMap<>();
		Map<String, Object> tokenFormatAsMap = new HashMap<>();
		tokenAsMap.put("@type", httpHeaders.getFirst("ids-securitytoken-type"));
		tokenAsMap.put("@id", httpHeaders.getFirst("ids-securitytoken-id"));
		tokenFormatAsMap.put("@id", httpHeaders.getFirst("ids-securitytoken-tokenformat"));
		tokenAsMap.put("ids:tokenFormat", tokenFormatAsMap);
		tokenAsMap.put("ids:tokenValue", httpHeaders.getFirst("ids-securitytoken-tokenvalue"));

		httpHeaders.remove("ids-securitytoken-type");
		httpHeaders.remove("ids-securitytoken-id");
		httpHeaders.remove("ids-securitytoken-tokenformat");
		httpHeaders.remove("ids-securitytoken-tokenvalue");

		return tokenAsMap;
	}
}

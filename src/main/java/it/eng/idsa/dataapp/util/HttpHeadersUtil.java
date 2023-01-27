package it.eng.idsa.dataapp.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarDeserializer;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarSerializer;
import it.eng.idsa.dataapp.web.rest.exceptions.InternalRecipientException;
import it.eng.idsa.multipart.util.UtilMessageService;

@Component
public class HttpHeadersUtil {

	private static final Logger logger = LoggerFactory.getLogger(HttpHeadersUtil.class);

	@SuppressWarnings("unchecked")
	public static Map<String, Object> messageToHttpHeaders(Message message) {
		if (logger.isDebugEnabled()) {
			logger.debug("Converting following message to http-headers: \r\n {}",
					UtilMessageService.getMessageAsString(message));
		}
		Map<String, Object> headers = new HashMap<>();
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
				headers.put("IDS-Id", message.getId().toString());
			} else if (entry.getKey().equals("@type")) {
				// when using Java it looks like this
				// headers.put("IDS-Messagetype", "ids:" +
				// message.getClass().getInterfaces()[1].getSimpleName());
				// for now we agreed to use the following, because of simplicity
				headers.put("IDS-Messagetype", entry.getValue());
			} else if (entry.getKey().equals("ids:securityToken")) {
				headers.put("IDS-SecurityToken-Type", ((Map<String, Object>) entry.getValue()).get("@type"));
				headers.put("IDS-SecurityToken-Id", message.getSecurityToken().getId().toString());
				headers.put("IDS-SecurityToken-TokenFormat", message.getSecurityToken().getTokenFormat().toString());
				headers.put("IDS-SecurityToken-TokenValue", message.getSecurityToken().getTokenValue());
			} else if (entry.getValue() instanceof Map) {
				Map<String, Object> valueMap = (Map<String, Object>) entry.getValue();
				if (valueMap.get("@id") != null) {
					headers.put(entry.getKey().replaceFirst("ids:", "IDS-").replaceFirst(entry.getKey().substring(4, 5),
							entry.getKey().substring(4, 5).toUpperCase()), valueMap.get("@id"));
				} else if (valueMap.get("@value") != null) {
					headers.put(entry.getKey().replaceFirst("ids:", "IDS-").replaceFirst(entry.getKey().substring(4, 5),
							entry.getKey().substring(4, 5).toUpperCase()), valueMap.get("@value"));
				}
			} else {
				headers.put(entry.getKey().replaceFirst("ids:", "IDS-").replaceFirst(entry.getKey().substring(4, 5),
						entry.getKey().substring(4, 5).toUpperCase()), entry.getValue());
			}
		});

		logger.debug("Message converted, following headers are the result: \r\n {}", headers.toString());

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

		if (logger.isDebugEnabled()) {
			logger.debug("Headers converted, following message is the result: \\r\\n {}",
					UtilMessageService.getMessageAsString(message));
		}

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

	public static HttpHeaders createResponseMessageHttpHeaders(Map<String, Object> httpHeadersReponse) {
		HttpHeaders headers = new HttpHeaders();
		Map<String, String> flatHttpHeadersReponse = httpHeadersReponse.entrySet().stream()
				.filter(entry -> entry.getValue() instanceof String)
				.collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));

		flatHttpHeadersReponse.forEach((key, value) -> {
			headers.add(key, value);
		});

		headers.add("foo", "bar");

		return headers;
	}
}

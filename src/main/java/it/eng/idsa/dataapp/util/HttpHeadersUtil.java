package it.eng.idsa.dataapp.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarDeserializer;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarSerializer;
import it.eng.idsa.multipart.util.UtilMessageService;

@Component
public class HttpHeadersUtil {

	private static final Logger logger = LoggerFactory.getLogger(HttpHeadersUtil.class);

	public HttpHeadersUtil() {
		super();
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> messageToHeaders(Message message) {
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

	@SuppressWarnings("unchecked")
	public Message headersToMessage(Map<String, Object> headers) {
		// bare in mind that in rumtime, headers is
		// org.apache.camel.util.CaseInsensitiveMap
		// which means that headers.get("aaa") is the same like headers.get("Aaa")
		logger.debug("Converting following http-headers to message: \r\n {}", headers.toString());

		Map<String, Object> messageAsHeader = new HashMap<>();

		ObjectMapper mapper = new ObjectMapper();
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addDeserializer(XMLGregorianCalendar.class, new XMLGregorianCalendarDeserializer());
		mapper.registerModule(simpleModule);

		// exclude null values from map
		mapper.setSerializationInclusion(Include.NON_NULL);

		Map<String, Object> tokeAsMap = null;
		if (headers.containsKey("ids-securitytoken-type")) {
			tokeAsMap = processDAPSTokenHeaders(headers);
		}
		String type = (String) headers.get("ids-messagetype");
		String id = (String) headers.get("ids-id");

		// handle recipientConnector - List
		List<URI> recipientConnector = new ArrayList<>();
		if (headers.containsKey("ids-recipientconnector")) {
			if (headers.get("ids-recipientconnector") instanceof String) {
				if (!headers.get("ids-recipientconnector").toString().equals("[]")) {
					recipientConnector.add(URI.create((String) headers.get("ids-recipientconnector")));
				}
			} else {
				recipientConnector = (List<URI>) headers.get("ids-recipientconnector");
			}
			headers.remove("ids-recipientconnector");
		}
		// handle recipientAgent - List
		List<URI> recipientAgent = new ArrayList<>();
		if (headers.containsKey("ids-recipientagent")) {
			if (headers.get("ids-recipientagent") instanceof String) {
				if (!headers.get("ids-recipientagent").toString().equals("[]")) {

					recipientAgent.add(URI.create((String) headers.get("ids-recipientagent")));
				}
			} else {
				recipientAgent = (List<URI>) headers.get("ids-recipientagent");
			}
			headers.remove("ids-recipientagent");
		}

		messageAsHeader = getIDSHeaders(headers);

		messageAsHeader.put("ids:securityToken", tokeAsMap);
		messageAsHeader.put("ids:recipientConnector", recipientConnector);
		messageAsHeader.put("ids:recipientAgent", recipientAgent);
		messageAsHeader.remove("ids:messagetype");
		messageAsHeader.remove("ids:id");
		messageAsHeader.put("@type", type);
		messageAsHeader.put("@id", id);

		headers.entrySet().removeIf(entry -> entry.getKey().startsWith("ids-") || entry.getKey().startsWith("ids:"));

		Message message = mapper.convertValue(messageAsHeader, Message.class);

		if (logger.isDebugEnabled()) {
			logger.debug("Headers converted, following message is the result: \\r\\n {}",
					UtilMessageService.getMessageAsString(message));
		}

		return message;
	}

	public Map<String, Object> getIDSHeaders(Map<String, Object> headers) {
		return headers.entrySet().stream().filter(e -> StringUtils.containsIgnoreCase(e.getKey(), "ids-"))
				.collect(java.util.stream.Collectors.toMap(e -> e.getKey().replaceFirst("ids-", "ids:")
						.replaceFirst(e.getKey().substring(4, 5), e.getKey().substring(4, 5).toLowerCase()),
						e -> e.getValue()));
	}

	private Map<String, Object> processDAPSTokenHeaders(Map<String, Object> headers) {
		Map<String, Object> tokenAsMap = new HashMap<>();
		Map<String, Object> tokenFormatAsMap = new HashMap<>();
		tokenAsMap.put("@type", headers.get("ids-securitytoken-type"));
		tokenAsMap.put("@id", headers.get("ids-securitytoken-id"));
		tokenFormatAsMap.put("@id", headers.get("ids-securitytoken-tokenformat"));
		tokenAsMap.put("ids:tokenFormat", tokenFormatAsMap);
		tokenAsMap.put("ids:tokenValue", headers.get("ids-securitytoken-tokenvalue"));

		headers.remove("ids-securitytoken-type");
		headers.remove("ids-securitytoken-id");
		headers.remove("ids-securitytoken-tokenformat");
		headers.remove("ids-securitytoken-tokenvalue");
		return tokenAsMap;
	}

	public Map<String, Object> httpHeadersToMap(HttpHeaders headers) {
		logger.debug("Converting HttpHeaders to map: \r\n {}", headers.toString());
		Map<String, Object> convertedHeaders = new HashMap<>();

		headers.forEach((key, value) -> {
			if (value.size() == 1) {
				convertedHeaders.put(key, value.get(0));
			} else {
				convertedHeaders.put(key, value);
			}
		});

		logger.debug("HttpHeaders converted, following map is the result: \r\n {}", convertedHeaders.toString());

		return convertedHeaders;
	}

	public HttpHeaders createResponseMessageHeaders(Map<String, Object> httpHeadersReponse) {
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

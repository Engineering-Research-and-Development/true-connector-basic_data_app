package it.eng.idsa.dataapp.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.domain.ProxyRequest;
import it.eng.idsa.dataapp.service.ProxyService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Service
public class ProxyServiceImpl implements ProxyService {
	private static final Object MULTIAPRT = "multipart";
	private static final String MESSAGE = "message";
	private static final String PAYLOAD = "payload";
	private static final String MESSAGE_AS_HEADERS = "messageAsHeaders";

	private static final Logger logger = LogManager.getLogger(ProxyService.class);

	private RestTemplate restTemplate;
	private ECCProperties eccProperties;
	
	public ProxyServiceImpl(RestTemplateBuilder restTemplateBuilder,  ECCProperties eccProperties) {
		this.restTemplate = restTemplateBuilder.build();
		this.eccProperties = eccProperties;
	}
	
	@Override
	@Deprecated
	public ResponseEntity<String> proxyMultipartMix(String body, HttpHeaders httpHeaders) 
			throws URISyntaxException {
		String header = getPayloadPart(body, MESSAGE);
		String payload = getPayloadPart(body, PAYLOAD);

		MultipartMessage mm = new MultipartMessageBuilder().withHeaderContent(header).withPayloadContent(payload).build();
		String proxyPayload = MultipartMessageProcessor.multipartMessagetoString(mm, false, true);
		URI thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
				eccProperties.getPort(), eccProperties.getMixContext(),
				null, null);
		HttpEntity<String> requestEntity = new HttpEntity<String>(proxyPayload, httpHeaders);
		logger.info("Forwarding mix POST request to {}", thirdPartyApi.toString());
		
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity, String.class);
		logResponse(resp);
		return resp;
	}

	@Override
	@Deprecated
	public ResponseEntity<String> proxyMultipartForm(String body, HttpHeaders httpHeaders) throws URISyntaxException {
		String header = getPayloadPart(body, MESSAGE);
		String payload = getPayloadPart(body, PAYLOAD);
		
		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		map.add("header", header);
		map.add("payload", payload);
		httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, httpHeaders);
		
		URI thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
				eccProperties.getPort(), eccProperties.getFormContext(),
				null, null);
		
		logger.info("Forwarding form POST request to {}", thirdPartyApi.toString());
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity, String.class);
		logResponse(resp);
		return resp;
	}

	@Override
	@Deprecated
	public ResponseEntity<String> proxyHttpHeader(String body, HttpHeaders httpHeaders) throws URISyntaxException {
		HttpEntity<String> requestEntity = new HttpEntity<String>(body, httpHeaders);
		
		URI thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
				eccProperties.getPort(), eccProperties.getHeaderContext(),
				null, null);

		logger.info("Forwarding header POST request to {}", thirdPartyApi.toString());
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity, String.class);
		logResponse(resp);
		return resp;
	}
	
	@Override
	public ProxyRequest parseIncommingProxyRequest(String body) {
		JSONParser parser = new JSONParser();
		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) parser.parse(body);
			
			String multipart =  (String) jsonObject.get(MULTIAPRT);
			
			JSONObject partJson = (JSONObject) jsonObject.get(MESSAGE);
			String message =  partJson != null ? partJson.toJSONString().replace("\\/","/") : null;
			
			partJson = (JSONObject) jsonObject.get(PAYLOAD);
			String payload =  partJson != null ? partJson.toJSONString().replace("\\/","/") : null;
			
			Map<String, Object> messageAsMap = (JSONObject) jsonObject.get(MESSAGE_AS_HEADERS);
			
			return new ProxyRequest(multipart, message, payload, messageAsMap);
		} catch (ParseException e) {
			logger.error("Error parsing payoad", e);
		}
		return new ProxyRequest();
	}

	private void logResponse(ResponseEntity<String> resp) {
		logger.info("Response received with status code {}", resp.getStatusCode());
		logger.info("Response headers\n{}", resp.getHeaders());
		logger.info("Response body\n{}", resp.getBody());
	}
	
	private String getPayloadPart(String payload, String part) {
		JSONParser parser=new JSONParser();
		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) parser.parse(payload);
			if("multipart".equals(part)) {
				return (String) jsonObject.get(part);
			}
			JSONObject partJson = (JSONObject) jsonObject.get(part);
			return partJson.toJSONString().replace("\\/","/");
		} catch (ParseException e) {
			logger.error("Error parsing payoad", e);
		}
		return null;
	}

	@Override
	public ResponseEntity<String> proxyMultipartMix(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException {
		
		if(StringUtils.isEmpty(proxyRequest.getMessage())) {
			logger.error("Missing '{}' part in the request", MESSAGE);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Message part in body is mandatory for " 
					+ proxyRequest.getMultipart() + " flow");
		}
		
		MultipartMessage mm = new MultipartMessageBuilder()
				.withHeaderContent(proxyRequest.getMessage())
				.withPayloadContent(proxyRequest.getPayload())
				.build();
		
		String proxyPayload = MultipartMessageProcessor.multipartMessagetoString(mm, false, true);
		URI thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
				eccProperties.getPort(), eccProperties.getMixContext(),
				null, null);
		HttpEntity<String> requestEntity = new HttpEntity<String>(proxyPayload, httpHeaders);
		logger.info("Forwarding mix POST request to {}", thirdPartyApi.toString());
		
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity, String.class);
		logResponse(resp);
		return resp;
	}

	@Override
	public ResponseEntity<String> proxyMultipartForm(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException {
		
		if(StringUtils.isEmpty(proxyRequest.getMessage())) {
			logger.error("Missing '{}' part in the request", MESSAGE);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Message part in body is mandatory for " 
					+ proxyRequest.getMultipart() + " flow");
		}
		
		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		map.add("header", proxyRequest.getMessage());
		map.add("payload", proxyRequest.getPayload());
		httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, httpHeaders);
		
		URI thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
				eccProperties.getPort(), eccProperties.getFormContext(),
				null, null);
		
		logger.info("Forwarding form POST request to {}", thirdPartyApi.toString());
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity, String.class);
		logResponse(resp);
		return resp;
	}

	@Override
	public ResponseEntity<String> proxyHttpHeader(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException {
		URI thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
				eccProperties.getPort(), eccProperties.getHeaderContext(),
				null, null);

		if(CollectionUtils.isEmpty(proxyRequest.getMessageAsHeader())) {
			logger.error("Missing '{}' part in the request", MESSAGE_AS_HEADERS);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MESSAGE_AS_HEADERS + " in body is mandatory for " 
					+ proxyRequest.getMultipart() + " flow");
		}
		logger.info("Forwarding header POST request to {}", thirdPartyApi.toString());
		proxyRequest.getMessageAsHeader().forEach((k,v) -> httpHeaders.add(k, (String) v));
		HttpEntity<String> requestEntity = new HttpEntity<>(proxyRequest.getPayload(), httpHeaders);
		
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity, String.class);
		logResponse(resp);
		return resp;
	}
}

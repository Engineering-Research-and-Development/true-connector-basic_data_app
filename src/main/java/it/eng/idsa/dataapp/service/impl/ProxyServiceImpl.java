package it.eng.idsa.dataapp.service.impl;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.service.ProxyService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Service
public class ProxyServiceImpl implements ProxyService {
	private static final String HEADER = "header";
	private static final String PAYLOAD = "payload";

	private static final Logger logger = LogManager.getLogger(ProxyService.class);

	private RestTemplate restTemplate;
	private ECCProperties eccProperties;
	
	public ProxyServiceImpl(RestTemplateBuilder restTemplateBuilder,  ECCProperties eccProperties) {
		this.restTemplate = restTemplateBuilder.build();
		this.eccProperties = eccProperties;
	}
	
	@Override
	public ResponseEntity<String> proxyMultipartMix(String body, HttpHeaders httpHeaders) 
			throws URISyntaxException {
		String header = getPayloadPart(body, HEADER);
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
	public ResponseEntity<String> proxyMultipartForm(String body, HttpHeaders httpHeaders) throws URISyntaxException {
		String header = getPayloadPart(body, HEADER);
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
			JSONObject partJson = (JSONObject) jsonObject.get(part);
			return partJson.toJSONString().replace("\\/","/");
		} catch (ParseException e) {
			logger.error("Error parsing payoad", e);
		}
		return null;
	}
}

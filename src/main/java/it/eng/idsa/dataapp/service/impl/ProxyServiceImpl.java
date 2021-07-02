package it.eng.idsa.dataapp.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.server.ResponseStatusException;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactRequestMessageBuilder;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.domain.ProxyRequest;
import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.dataapp.service.ProxyService;
import it.eng.idsa.dataapp.service.RecreateFileService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.streamer.WebSocketClientManager;
import it.eng.idsa.streamer.util.MultiPartMessageServiceUtil;
import it.eng.idsa.streamer.websocket.receiver.server.FileRecreatorBeanExecutor;

@Service
public class ProxyServiceImpl implements ProxyService {
	private static final String MULTIPART = "multipart";
	private static final String MESSAGE = "message";
	private static final String PAYLOAD = "payload";
	private static final String REQUESTED_ARTIFACT = "requestedArtifact";
	private static final String FORWARD_TO = "Forward-To";
	private static final String FORWARD_TO_INTERNAL = "Forward-To-Internal";

	private static final String MESSAGE_AS_HEADERS = "messageAsHeaders";

	private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);

	private RestTemplate restTemplate;
	private ECCProperties eccProperties;
	private MultiPartMessageService multiPartMessageService;
	private RecreateFileService recreateFileService;
	private String dataLakeDirectory;
	
	public ProxyServiceImpl(RestTemplateBuilder restTemplateBuilder,  ECCProperties eccProperties,
			MultiPartMessageService multiPartMessageService, RecreateFileService recreateFileService,
			@Value("${application.dataLakeDirectory}") String dataLakeDirectory) {
		this.restTemplate = restTemplateBuilder.build();
		this.eccProperties = eccProperties;
		this.multiPartMessageService = multiPartMessageService;
		this.recreateFileService = recreateFileService;
		this.dataLakeDirectory = dataLakeDirectory;
	}
	
	@Override
	public ProxyRequest parseIncommingProxyRequest(String body) {
		JSONParser parser = new JSONParser();
		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) parser.parse(body);
			
			String multipart =  (String) jsonObject.get(MULTIPART);
			
			String forwardTo =  (String) jsonObject.get(FORWARD_TO);
			
			String forwardToInternal =  (String) jsonObject.get(FORWARD_TO_INTERNAL);
			
			String requestedArtifact = (String) jsonObject.get(REQUESTED_ARTIFACT);
			
			JSONObject partJson = (JSONObject) jsonObject.get(MESSAGE);
			String message =  partJson != null ? partJson.toJSONString().replace("\\/","/") : null;
			
			String payload = null;
			if(jsonObject.get(PAYLOAD) instanceof String) {
				payload = ((String) jsonObject.get(PAYLOAD));//.replace("\\/","/").replace("\\", "");
			} else {
				JSONObject partJsonPayload = (JSONObject) jsonObject.get(PAYLOAD);
				payload =  partJsonPayload != null ? partJsonPayload.toJSONString().replace("\\/","/") : null;
			}
			
			Map<String, Object> messageAsMap = (JSONObject) jsonObject.get(MESSAGE_AS_HEADERS);
			
			return new ProxyRequest(multipart, forwardTo, forwardToInternal, message, payload, requestedArtifact, messageAsMap);
		} catch (ParseException e) {
			logger.error("Error parsing payoad", e);
		}
		return new ProxyRequest();
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
		httpHeaders.add(FORWARD_TO, proxyRequest.getForwardTo());
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
		httpHeaders.add(FORWARD_TO, proxyRequest.getForwardTo());
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
		httpHeaders.add(FORWARD_TO, proxyRequest.getForwardTo());
		HttpEntity<String> requestEntity = new HttpEntity<>(proxyRequest.getPayload(), httpHeaders);
		
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity, String.class);
		logResponse(resp);
		return resp;
	}

	@Override
	public ResponseEntity<String> requestArtifact(ProxyRequest proxyRequest){
		String forwardToInternal = proxyRequest.getForwardToInternal();
		String forwardTo = proxyRequest.getForwardTo();
		
		if(StringUtils.isEmpty(forwardTo) || StringUtils.isEmpty(forwardToInternal)) {
			return ResponseEntity.badRequest().body("Missing required fields Forward-To or Forward-To-Internal");
		}
		
		URI requestedArtifactURI = URI
				.create("http://w3id.org/engrd/connector/artifact/" + proxyRequest.getRequestedArtifact());
		Message artifactRequestMessage;
		try {
			artifactRequestMessage = new ArtifactRequestMessageBuilder()
					._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
					._issuerConnector_(URI.create("http://w3id.org/engrd/connector"))
					._modelVersion_("4.0.0")
					._requestedArtifact_(requestedArtifactURI)
					.build();

			Serializer serializer = new Serializer();
			String requestMessage = serializer.serialize(artifactRequestMessage);
			FileRecreatorBeanExecutor.getInstance().setForwardTo(forwardTo);
			String responseMessage = WebSocketClientManager.getMessageWebSocketSender()
					.sendMultipartMessageWebSocketOverHttps(requestMessage, proxyRequest.getPayload(), forwardToInternal);
			
			String fileNameSaved = saveFileToDisk(responseMessage, artifactRequestMessage);
			
			if(fileNameSaved != null) {
				return ResponseEntity.ok("{​​\"message\":\"File '" + fileNameSaved + "' created successfully\"}");
			}
			return ResponseEntity.ok(responseMessage);
		} catch (Exception exc) {
			logger.error("Error while processing request {}", exc);
			 throw new ResponseStatusException(
			           HttpStatus.INTERNAL_SERVER_ERROR, 
			           "Error while processing request, check logs for more details", 
			           exc);
		}
	}
	
	private void logResponse(ResponseEntity<String> resp) {
		logger.info("Response received with status code {}", resp.getStatusCode());
		logger.info("Response headers\n{}", resp.getHeaders());
		logger.info("Response body\n{}", resp.getBody());
	}
	
	// TODO should we move this method to separate class?
	private String saveFileToDisk(String responseMessage, Message requestMessage) throws IOException {
		Message responseMsg = multiPartMessageService.getMessage(responseMessage);

		String requestedArtifact = null;
		if (requestMessage instanceof ArtifactRequestMessage && responseMsg instanceof ArtifactResponseMessage) {
			String payload = MultiPartMessageServiceUtil.getPayload(responseMessage);
			String reqArtifact = ((ArtifactRequestMessage) requestMessage).getRequestedArtifact().getPath();
			// get resource from URI http://w3id.org/engrd/connector/artifact/ + requestedArtifact
			requestedArtifact = reqArtifact.substring(reqArtifact.lastIndexOf('/') + 1);
			String dataLake = dataLakeDirectory + FileSystems.getDefault().getSeparator() + requestedArtifact;
			logger.info("About to save file " + dataLake);
			recreateFileService.recreateTheFile(payload, new File(dataLake));
			logger.info("File saved");
		} else {
			logger.info("Did not have ArtifactRequestMessage and ResponseMessage - nothing to save");
			requestedArtifact = null;
		}
		return requestedArtifact;
	}

	@Override
	public ResponseEntity<String> proxyWSSRequest(ProxyRequest proxyRequest) {
		String forwardToInternal = proxyRequest.getForwardToInternal();
		String forwardTo = proxyRequest.getForwardTo();
		
		if(StringUtils.isEmpty(forwardTo) || StringUtils.isEmpty(forwardToInternal)) {
			return ResponseEntity.badRequest().body("Missing required fields Forward-To or Forward-To-Internal");
		}
		
		FileRecreatorBeanExecutor.getInstance().setForwardTo(forwardTo);
		String responseMessage = null;
		try {
			responseMessage = WebSocketClientManager.getMessageWebSocketSender()
					.sendMultipartMessageWebSocketOverHttps(proxyRequest.getMessage(), proxyRequest.getPayload(), forwardToInternal);
		} catch (Exception exc) {
			logger.error("Error while processing request {}", exc);
			 throw new ResponseStatusException(
			           HttpStatus.INTERNAL_SERVER_ERROR, 
			           "Error while processing request, check logs for more details", 
			           exc);
		}
		
		return ResponseEntity.ok(responseMessage);
	}
}

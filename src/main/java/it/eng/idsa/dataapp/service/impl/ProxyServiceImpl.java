package it.eng.idsa.dataapp.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.util.UUID;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactRequestMessageBuilder;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessage;
import de.fraunhofer.iais.eis.ConnectorUpdateMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.QueryMessage;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.domain.ProxyRequest;
import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.dataapp.service.ProxyService;
import it.eng.idsa.dataapp.service.RecreateFileService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;
import it.eng.idsa.streamer.WebSocketClientManager;
import it.eng.idsa.streamer.util.MultiPartMessageServiceUtil;
import it.eng.idsa.streamer.websocket.receiver.server.FileRecreatorBeanExecutor;

@Service
public class ProxyServiceImpl implements ProxyService {
	private static final String MULTIPART = "multipart";
	private static final String PAYLOAD = "payload";
	private static final String REQUESTED_ARTIFACT = "requestedArtifact";
	private static final String FORWARD_TO = "Forward-To";
	private static final String FORWARD_TO_INTERNAL = "Forward-To-Internal";
	private static final String MESSAGE_TYPE = "messageType";
	private static final String REQUESTED_ELEMENT = "requestedElement";


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
			String messageType = (String) jsonObject.get(MESSAGE_TYPE);
			String requestedElement = (String) jsonObject.get(REQUESTED_ELEMENT);
			
			String payload = null;
			if(jsonObject.get(PAYLOAD) instanceof String) {
				payload = ((String) jsonObject.get(PAYLOAD)).replace("\\/","/").replace("\\", "");
			} else {
				JSONObject partJson = (JSONObject) jsonObject.get(PAYLOAD);
				payload =  partJson != null ? partJson.toJSONString().replace("\\/","/") : null;
			}
			
			return new ProxyRequest(multipart, forwardTo, forwardToInternal, payload, requestedArtifact, messageType, requestedElement);
		} catch (ParseException e) {
			logger.error("Error parsing payoad", e);
		}
		return new ProxyRequest();
	}

	@Override
	public ResponseEntity<String> proxyMultipartMix(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException {
		
		if(StringUtils.isEmpty(proxyRequest.getMessageType())) {
			logger.error("Missing '{}' part in the request", MESSAGE_TYPE);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Message type part in body is mandatory for " 
					+ proxyRequest.getMultipart() + " flow");
		}
		URI thirdPartyApi = null;
		String proxyPayload = null;
		httpHeaders.add(FORWARD_TO, proxyRequest.getForwardTo());

		Message requestMessage = createRequestMessage(proxyRequest.getMessageType(), proxyRequest.getRequestedArtifact(), proxyRequest.getRequestedElement());
		
		if(requestMessage != null) {
			MultipartMessage mm = new MultipartMessageBuilder()
					.withHeaderContent(requestMessage)
					.withPayloadContent(proxyRequest.getPayload())
					.build();
			proxyPayload = MultipartMessageProcessor.multipartMessagetoString(mm, false, true);
			
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getMixContext(),
					null, null);
			
		} else if (ConnectorUpdateMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUpdateMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerRegisterContext(),
					null, null);
		} else if (ConnectorUnavailableMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUnavailableMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerDeleteContext(),
					null, null);
		} else if (QueryMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - QueryMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerQuerryContext(),
					null, null);
			proxyPayload = proxyRequest.getPayload();
		}
		
		logger.info("Forwarding mix POST request to {}", thirdPartyApi.toString());
		
		HttpEntity<String> requestEntity = new HttpEntity<String>(proxyPayload, httpHeaders);
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity, String.class);
		logResponse(resp);
		return resp;
	}

	@Override
	public ResponseEntity<String> proxyMultipartForm(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException {
		
		if(StringUtils.isEmpty(proxyRequest.getMessageType())) {
			logger.error("Missing '{}' part in the request", MESSAGE_TYPE);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Message part in body is mandatory for " 
					+ proxyRequest.getMultipart() + " flow");
		}
		
		Message requestMessage = createRequestMessage(proxyRequest.getMessageType(), proxyRequest.getRequestedArtifact(), proxyRequest.getRequestedElement());
		URI thirdPartyApi = null;
		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = null;
		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		httpHeaders.add(FORWARD_TO, proxyRequest.getForwardTo());
		httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

		if (requestMessage != null) {
			map.add("header", UtilMessageService.getMessageAsString(requestMessage));
			map.add("payload", proxyRequest.getPayload());
			
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getFormContext(),
					null, null);
		} else if (ConnectorUpdateMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUpdateMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerRegisterContext(),
					null, null);
		} else if (ConnectorUnavailableMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUnavailableMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerDeleteContext(),
					null, null);
		} else if (QueryMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - QueryMessage");
			map.add("payload", proxyRequest.getPayload());
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerQuerryContext(),
					null, null);
		}
		
		logger.info("Forwarding form POST request to {}", thirdPartyApi.toString());
		requestEntity = new HttpEntity<>(map, httpHeaders);
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity, String.class);
		logResponse(resp);
		return resp;
	}

	private Message createRequestMessage(String messageType, String requestedArtifact, String requestedElement) {
		if(ArtifactRequestMessage.class.getSimpleName().equals(messageType)) {
			return UtilMessageService.getArtifactRequestMessage(requestedArtifact != null 
					? URI.create(requestedArtifact) 
							: UtilMessageService.REQUESTED_ARTIFACT);
		} else if(ContractAgreementMessage.class.getSimpleName().equals(messageType)) {
			return UtilMessageService.getContractAgreementMessage();
		} else if(DescriptionRequestMessage.class.getSimpleName().equals(messageType)) {
			return UtilMessageService.getDescriptionRequestMessage(URI.create(requestedElement));
		} 
		return null;
	}

	@Override
	public ResponseEntity<String> proxyHttpHeader(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException {
		URI thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
				eccProperties.getPort(), eccProperties.getHeaderContext(),
				null, null);

		if(StringUtils.isBlank(proxyRequest.getMessageType())) {
			logger.error("Missing '{}' part in the request", MESSAGE_TYPE);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MESSAGE_TYPE + " in body is mandatory for " 
					+ proxyRequest.getMultipart() + " flow");
		}
		logger.info("Forwarding header POST request to {}", thirdPartyApi.toString());
		httpHeaders.addAll(createMessageAsHeader(proxyRequest.getMessageType(), proxyRequest.getRequestedArtifact(), proxyRequest.getRequestedElement()));
		httpHeaders.add(FORWARD_TO, proxyRequest.getForwardTo());
		HttpEntity<String> requestEntity = new HttpEntity<>(proxyRequest.getPayload(), httpHeaders);
		
		if (ConnectorUpdateMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUpdateMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerRegisterContext(),
					null, null);
		} else if (ConnectorUnavailableMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
		 	logger.info("Broker message - ConnectorUnavailableMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerDeleteContext(),
					null, null);
		} else if (QueryMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - QueryMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerQuerryContext(),
					null, null);
		}
		
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity, String.class);
		logResponse(resp);
		return resp;
	}

	private HttpHeaders createMessageAsHeader(String messageType, String requestedArtifact, String requestedElement) {
		HttpHeaders httpHeaders = new HttpHeaders();
		if(ArtifactRequestMessage.class.getSimpleName().equals(messageType)) {
			httpHeaders.add("IDS-Messagetype", "ids:ArtifactRequestMessage");
			httpHeaders.add("IDS-Id", "https://w3id.org/idsa/autogen/" + ArtifactRequestMessage.class.getSimpleName() + "/" + UUID.randomUUID());
			httpHeaders.add("IDS-RequestedArtifact", requestedArtifact != null ? requestedArtifact : UtilMessageService.REQUESTED_ARTIFACT.toString());
		} else if(ContractAgreementMessage.class.getSimpleName().equals(messageType)) {
			httpHeaders.add("IDS-Messagetype", "ids:ContractAgreementMessage");
			httpHeaders.add("IDS-Id", "https://w3id.org/idsa/autogen/" + ContractAgreementMessage.class.getSimpleName() + "/" + UUID.randomUUID());
		} else if(DescriptionRequestMessage.class.getSimpleName().equals(messageType)) {
			httpHeaders.add("IDS-Messagetype", "ids:DescriptionRequestMessage");
			httpHeaders.add("IDS-Id", "https://w3id.org/idsa/autogen/" + DescriptionRequestMessage.class.getSimpleName() + "/" + UUID.randomUUID());
			httpHeaders.add("IDS-RequestedElement", requestedElement);
		}
		httpHeaders.add("IDS-ModelVersion", "4.1.1");
		httpHeaders.add("IDS-Issued", DateUtil.now().toXMLFormat());
		httpHeaders.add("IDS-IssuerConnector", "http://w3id.org/engrd/connector/");
		httpHeaders.add("IDS-SenderAgent", "http://sender.agent.com/");
		
		httpHeaders.add("IDS-SecurityToken-Type", "ids:DynamicAttributeToken");
		httpHeaders.add("IDS-SecurityToken-Id", "https://w3id.org/idsa/autogen/" + UUID.randomUUID());
		httpHeaders.add("IDS-SecurityToken-TokenFormat", TokenFormat.JWT.getId().toString());
		httpHeaders.add("IDS-SecurityToken-TokenValue", UtilMessageService.TOKEN_VALUE);
		
		return httpHeaders;
	}

	@Override
	public ResponseEntity<String> requestArtifact(ProxyRequest proxyRequest){
		String forwardToInternal = proxyRequest.getForwardToInternal();
		String forwardTo = proxyRequest.getForwardTo();
		logger.info("Proxying wss ArtifactRequestMessage...");
		
		if(StringUtils.isEmpty(forwardTo) || StringUtils.isEmpty(forwardToInternal)) {
			return ResponseEntity.badRequest().body("Missing required fields Forward-To or Forward-To-Internal");
		}
		
		URI requestedArtifactURI = URI
				.create("http://w3id.org/engrd/connector/artifact/" + proxyRequest.getRequestedArtifact());
		Message artifactRequestMessage;
		try {
			artifactRequestMessage = new ArtifactRequestMessageBuilder()
					._issued_(DateUtil.now())
					._issuerConnector_(URI.create("http://w3id.org/engrd/connector"))
					._modelVersion_("4.0.6")
					._requestedArtifact_(requestedArtifactURI)
					._securityToken_(UtilMessageService.getDynamicAttributeToken())
					._senderAgent_(URI.create("https://sender.agent.com"))
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
		logger.info("Proxying wss request ...");
		if(StringUtils.isEmpty(forwardTo) || StringUtils.isEmpty(forwardToInternal)) {
			return ResponseEntity.badRequest().body("Missing required fields Forward-To or Forward-To-Internal");
		}
		
		FileRecreatorBeanExecutor.getInstance().setForwardTo(forwardTo);
		String responseMessage = null;
		try {
			Message requestMessage = createRequestMessage(proxyRequest.getMessageType(), proxyRequest.getRequestedArtifact(), proxyRequest.getRequestedElement());
			responseMessage = WebSocketClientManager.getMessageWebSocketSender()
					.sendMultipartMessageWebSocketOverHttps(UtilMessageService.getMessageAsString(requestMessage), 
							proxyRequest.getPayload(), forwardToInternal);
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

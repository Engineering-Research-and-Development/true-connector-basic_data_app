package it.eng.idsa.dataapp.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
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
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import de.fraunhofer.iais.eis.ArtifactImpl;
import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactRequestMessageBuilder;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessage;
import de.fraunhofer.iais.eis.ConnectorUpdateMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessageBuilder;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.ContractRequestMessageBuilder;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessageBuilder;
import de.fraunhofer.iais.eis.DescriptionResponseMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.QueryMessage;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.domain.ProxyRequest;
import it.eng.idsa.dataapp.ftp.client.FTPClient;
import it.eng.idsa.dataapp.service.CheckSumService;
import it.eng.idsa.dataapp.service.ProxyService;
import it.eng.idsa.dataapp.service.RecreateFileService;
import it.eng.idsa.dataapp.util.MessageUtil;
import it.eng.idsa.dataapp.util.RejectionUtil;
import it.eng.idsa.dataapp.web.rest.exceptions.InternalRecipientException;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;
import it.eng.idsa.streamer.WebSocketClientManager;
import it.eng.idsa.streamer.websocket.receiver.server.FileRecreatorBeanExecutor;

@Service
public class ProxyServiceImpl implements ProxyService {
	private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);

	private static final String MULTIPART = "multipart";
	private static final String PAYLOAD = "payload";
	private static final String REQUESTED_ARTIFACT = "requestedArtifact";
	private static final String FORWARD_TO = "Forward-To";
	private static final String FORWARD_TO_INTERNAL = "Forward-To-Internal";
	private static final String MESSAGE_TYPE = "messageType";
	private static final String REQUESTED_ELEMENT = "requestedElement";
	private static final String TRANSFER_CONTRACT = "transferContract";

	@Value("${application.verifyCheckSum}")
	private boolean verifyCheckSum;

	private RestTemplate restTemplate;
	private ECCProperties eccProperties;
	private RecreateFileService recreateFileService;
	private String dataLakeDirectory;
	private String issueConnector;
	private Boolean encodePayload;
	private Boolean extractPayloadFromResponse;
	private Optional<CheckSumService> checkSumService;
	private SelfDescriptionValidator selfDescriptionValidator;
	private FTPClient ftpClient;
	private JSONParser parser = new JSONParser();

	/**
	 * 
	 * ProxyServiceImpl constructor
	 * 
	 * Use this as an example how to setup RestTemplate with RestTemplateBuilder
	 * 
	 * @param restTemplateBuilder        REST Template builder
	 * @param eccProperties              properties for ECC configuration
	 * @param recreateFileService        RecreateFileService for wss communication
	 * @param checkSumService            CheckSumService for verifying checksum
	 * @param dataLakeDirectory          Data lake directory for file persistence
	 * @param issuerConnector            issuer connector Id
	 * @param encodePayload              encode payload
	 * @param extractPayloadFromResponse extract payload from multipart
	 * @param selfDescriptionValidator   payloadHandler
	 */
	public ProxyServiceImpl(RestTemplateBuilder restTemplateBuilder, ECCProperties eccProperties,
			RecreateFileService recreateFileService, Optional<CheckSumService> checkSumService,
			@Value("${application.dataLakeDirectory}") String dataLakeDirectory,
			@Value("${application.ecc.issuer.connector}") String issuerConnector,
			@Value("#{new Boolean('${application.encodePayload:false}')}") Boolean encodePayload,
			@Value("#{new Boolean('${application.extractPayloadFromResponse:false}')}") Boolean extractPayloadFromResponse,
			SelfDescriptionValidator selfDescriptionValidator, FTPClient ftpClient) {
		this.restTemplate = restTemplateBuilder.build();
		this.eccProperties = eccProperties;
		this.recreateFileService = recreateFileService;
		this.checkSumService = checkSumService;
		this.dataLakeDirectory = dataLakeDirectory;
		this.issueConnector = issuerConnector;
		this.encodePayload = encodePayload;
		this.extractPayloadFromResponse = extractPayloadFromResponse;
		this.selfDescriptionValidator = selfDescriptionValidator;
		this.ftpClient = ftpClient;
	}

	@Override
	public ProxyRequest parseIncommingProxyRequest(String body) {
		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) parser.parse(body);

			String multipart = (String) jsonObject.get(MULTIPART);
			String forwardTo = (String) jsonObject.get(FORWARD_TO);
			String forwardToInternal = (String) jsonObject.get(FORWARD_TO_INTERNAL);
			String requestedArtifact = (String) jsonObject.get(REQUESTED_ARTIFACT);
			String messageType = (String) jsonObject.get(MESSAGE_TYPE);
			String requestedElement = (String) jsonObject.get(REQUESTED_ELEMENT);
			String transferContract = (String) jsonObject.get(TRANSFER_CONTRACT);

			String payload = null;
			if (jsonObject.get(PAYLOAD) instanceof String) {
				payload = ((String) jsonObject.get(PAYLOAD)).replace("\\/", "/").replace("\\", "");
			} else {
				JSONObject partJson = (JSONObject) jsonObject.get(PAYLOAD);
				payload = partJson != null ? partJson.toJSONString().replace("\\/", "/") : null;
			}

			return new ProxyRequest(multipart, forwardTo, forwardToInternal, payload, requestedArtifact, messageType,
					requestedElement, transferContract);
		} catch (ParseException e) {
			logger.error("Error parsing payoad", e);
		}
		return new ProxyRequest();
	}

	@Override
	public ResponseEntity<String> proxyMultipartMix(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException {

		URI thirdPartyApi = null;
		String proxyPayload = null;
		httpHeaders.add(FORWARD_TO, proxyRequest.getForwardTo());

		Message requestMessage = createRequestMessage(proxyRequest);
		if (logger.isDebugEnabled()) {
			logger.debug("Created request message {}", UtilMessageService.getMessageAsString(requestMessage));
		}

		if (requestMessage != null) {
			String payload = null;
			if (requestMessage instanceof ContractRequestMessage && proxyRequest.getPayload() == null) {
				logger.info("Creating ContractRequest for payload using requested artifact");
				payload = UtilMessageService.getMessageAsString(UtilMessageService.getContractRequest(
						URI.create(proxyRequest.getRequestedElement()), URI.create("http://permission.id")));
			} else {
				logger.info("Using payload from request");
				payload = proxyRequest.getPayload();
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Created payload {}", payload);
			}
			MultipartMessage mm = new MultipartMessageBuilder().withHeaderContent(requestMessage)
					.withPayloadContent(payload).build();
			proxyPayload = MultipartMessageProcessor.multipartMessagetoString(mm, false, true);

			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
					eccProperties.getMixContext(), null, null);

		} else if (ConnectorUpdateMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUpdateMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
					eccProperties.getBrokerRegisterContext(), null, null);
		} else if (ConnectorUnavailableMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUnavailableMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
					eccProperties.getBrokerDeleteContext(), null, null);
		} else if (QueryMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - QueryMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
					eccProperties.getBrokerQuerryContext(), null, null);
			proxyPayload = proxyRequest.getPayload();
		}

		logger.info("Forwarding mix POST request to {}", thirdPartyApi.toString());

		HttpEntity<String> requestEntity = new HttpEntity<String>(proxyPayload, httpHeaders);

		return sendMultipartRequest(thirdPartyApi, requestEntity, proxyRequest);
	}

	@Override
	public ResponseEntity<String> proxyMultipartForm(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException {

		Message requestMessage = createRequestMessage(proxyRequest);
		if (logger.isDebugEnabled()) {
			logger.debug("Created request message {}", UtilMessageService.getMessageAsString(requestMessage));
		}
		URI thirdPartyApi = null;
		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = null;
		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		httpHeaders.add(FORWARD_TO, proxyRequest.getForwardTo());
		httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

		if (requestMessage != null) {
			map.add("header", UtilMessageService.getMessageAsString(requestMessage));
			String payload = null;
			if (requestMessage instanceof ContractRequestMessage && proxyRequest.getPayload() == null) {
				logger.info("Creating ContractRequest for payload using requested artifact");
				payload = UtilMessageService.getMessageAsString(UtilMessageService.getContractRequest(
						URI.create(proxyRequest.getRequestedElement()), URI.create("http://permission.id")));
			} else {
				logger.info("Using payload from request");
				payload = proxyRequest.getPayload();
			}
			map.add("payload", payload);
			if (logger.isDebugEnabled()) {
				logger.debug("Created payload {}", payload);
			}
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
					eccProperties.getFormContext(), null, null);
		} else if (ConnectorUpdateMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUpdateMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
					eccProperties.getBrokerRegisterContext(), null, null);
		} else if (ConnectorUnavailableMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUnavailableMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
					eccProperties.getBrokerDeleteContext(), null, null);
		} else if (QueryMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - QueryMessage");
			map.add("payload", proxyRequest.getPayload());
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
					eccProperties.getBrokerQuerryContext(), null, null);
		}

		requestEntity = new HttpEntity<>(map, httpHeaders);

		logger.info("Forwarding form POST request to {}", thirdPartyApi.toString());
		return sendMultipartRequest(thirdPartyApi, requestEntity, proxyRequest);
	}

	public Long fetchChecksum(String forwardTo, String artifactId) throws URISyntaxException, IOException {

		MultipartMessage mm = fetchSelfDescription(forwardTo, null);
		Connector baseConnector = null;
		// USe atomic because of an access in lambdas
		AtomicLong checkSum = new AtomicLong(0L);

		baseConnector = new Serializer().deserialize(mm.getPayloadContent(), Connector.class);

		baseConnector.getResourceCatalog().forEach(c -> {
			if (!c.getOfferedResource().isEmpty()) {
				c.getOfferedResource().forEach(r -> {
					r.getRepresentation().forEach(rp -> {
						rp.getInstance().forEach(instance -> {
							if (StringUtils.equals(instance.getId().toString(), artifactId)) {
								ArtifactImpl artifact = null;
								try {
									artifact = new Serializer().deserialize(instance.toString(), ArtifactImpl.class);
								} catch (IOException e) {
									logger.error("Following error occured: {}", e);
									throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
											"Error while processing request, check logs for more details", e);

								}
								if (artifact.getCheckSum() != null) {
									checkSum.set(Long.parseLong(artifact.getCheckSum()));
									logger.info("CheckSum fetched");

								} else {
									logger.info("Artifact doesn't have checkSum");
								}
							}
						});
					});
				});
			} else {
				MultipartMessage mm2 = null;

				try {
					mm2 = fetchSelfDescription(forwardTo, c.getId().toString());
				} catch (URISyntaxException exception) {
					logger.error("Error while processing request {}", exception);
					throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
							"Error while processing request, check logs for more details", exception);
				}

				try {
					ResourceCatalog catalog = new Serializer().deserialize(mm2.getPayloadContent(),
							ResourceCatalog.class);
					catalog.getOfferedResource().forEach(resource -> {
						resource.getRepresentation().forEach(representation -> {
							representation.getInstance().forEach(instance -> {
								if (StringUtils.equals(instance.getId().toString(), artifactId)) {
									ArtifactImpl artifact = null;
									try {
										artifact = new Serializer().deserialize(instance.toString(),
												ArtifactImpl.class);
									} catch (IOException e) {
										logger.error("Following error occured: {}", e);
										throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
												"Error while processing request, check logs for more details", e);

									}
									if (artifact.getCheckSum() != null) {
										checkSum.set(Long.parseLong(artifact.getCheckSum()));
										logger.info("CheckSum stored");

									} else {
										logger.info("Artifact doesn't have checkSum");
									}
								}
							});
						});
					});
				} catch (IOException e) {
					logger.error("Error while processing request {}", e);
					throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
							"Error while processing request, check logs for more details", e);
				}

			}
		});
		return checkSum.get();
	}

	private MultipartMessage fetchSelfDescription(String forwardTo, String requestedElement) throws URISyntaxException {
		HttpHeaders httpHeaders = new HttpHeaders();
		DescriptionRequestMessage descriptionRequestMessage;
		if (requestedElement == null) {
			descriptionRequestMessage = new DescriptionRequestMessageBuilder()._issued_(UtilMessageService.ISSUED)
					._issuerConnector_(URI.create(issueConnector))._modelVersion_(UtilMessageService.MODEL_VERSION)
					._requestedElement_(null)._senderAgent_(UtilMessageService.SENDER_AGENT)
					._securityToken_(UtilMessageService.getDynamicAttributeToken()).build();
		} else {
			descriptionRequestMessage = new DescriptionRequestMessageBuilder()._issued_(UtilMessageService.ISSUED)
					._issuerConnector_(URI.create(issueConnector))._modelVersion_(UtilMessageService.MODEL_VERSION)
					._requestedElement_(URI.create(requestedElement))._senderAgent_(UtilMessageService.SENDER_AGENT)
					._securityToken_(UtilMessageService.getDynamicAttributeToken()).build();

		}
		URI thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
				eccProperties.getFormContext(), null, null);

		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		map.add("header", UtilMessageService.getMessageAsString(descriptionRequestMessage));
		map.add("payload", null);

		httpHeaders.add(FORWARD_TO, forwardTo);
		httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, httpHeaders);
		try {
			ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity,
					String.class);
			MultipartMessage mm = MultipartMessageProcessor.parseMultipartMessage(resp.getBody());
			return mm;
		} catch (RestClientException e) {
			logger.error("Following error occurred: {}", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Error while processing request, check logs for more details", e);
		}

	}

	@Override
	public ResponseEntity<String> proxyHttpHeader(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException {
		URI thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
				eccProperties.getHeaderContext(), null, null);

		logger.info("Forwarding header POST request to {}", thirdPartyApi.toString());
		httpHeaders.addAll(createMessageAsHeader(proxyRequest));
		httpHeaders.add(FORWARD_TO, proxyRequest.getForwardTo());

		String payload = null;
		if (proxyRequest.getMessageType().contains("ContractRequestMessage") && proxyRequest.getPayload() == null) {
			logger.info("Creating ContractRequest for payload using requested artifact");
			payload = UtilMessageService.getMessageAsString(UtilMessageService.getContractRequest(
					URI.create(proxyRequest.getRequestedElement()), URI.create("http://permission.id")));
		} else {
			logger.info("Using payload from request");
			payload = proxyRequest.getPayload();
		}
		HttpEntity<String> requestEntity = new HttpEntity<>(payload, httpHeaders);

		if (ConnectorUpdateMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUpdateMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
					eccProperties.getBrokerRegisterContext(), null, null);
		} else if (ConnectorUnavailableMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUnavailableMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
					eccProperties.getBrokerDeleteContext(), null, null);
		} else if (QueryMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - QueryMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
					eccProperties.getBrokerQuerryContext(), null, null);
		}

		return sendHttpHeadersRequest(thirdPartyApi, requestEntity);
	}

	private ResponseEntity<String> sendHttpHeadersRequest(URI thirdPartyApi, HttpEntity<String> requestEntity) {
		try {
			ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity,
					String.class);
			logHttpHeadersResponse(resp);
			return handleHttpHeadersResponse(resp);
		} catch (RestClientException e) {
			logger.error("Following error occured: {}", e);
			return new ResponseEntity<String>("Message could not be processed", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private ResponseEntity<String> handleHttpHeadersResponse(ResponseEntity<String> resp) {
		if (resp.getHeaders().size() != 0 && StringUtils.isNotBlank(resp.getHeaders().get("IDS-Messagetype").get(0))
				&& "ids:RejectionMessage".equals(resp.getHeaders().get("IDS-Messagetype").get(0))) {
			if (StringUtils.isBlank(resp.getHeaders().get("IDS-RejectionReason").get(0))) {
				return new ResponseEntity<String>("Error while processing message", HttpStatus.BAD_REQUEST);
			}

			return RejectionUtil.HANDLE_REJECTION(RejectionReason.valueOf(resp.getHeaders().get("IDS-RejectionReason")
					.get(0).substring(resp.getHeaders().get("IDS-RejectionReason").get(0).lastIndexOf("/") + 1)));
		}

		if (resp.getHeaders().size() != 0 && StringUtils.isNotBlank(resp.getHeaders().get("IDS-Messagetype").get(0))
				&& "ids:DescriptionMessageResponse".equals(resp.getHeaders().get("IDS-Messagetype").get(0))) {
			boolean selfDescriptionValid = selfDescriptionValidator.validateSelfDescription(resp.getBody());
			if (!selfDescriptionValid) {
				return new ResponseEntity<String>("Invalid self description received - check logs for more details",
						HttpStatus.BAD_REQUEST);
			}
		}
		if (extractPayloadFromResponse) {
			if (resp.getHeaders().size() != 0 && StringUtils.isNotBlank(resp.getHeaders().get("IDS-Messagetype").get(0))
					&& "ids:MessageProcessedNotificationMessage"
							.equals(resp.getHeaders().get("IDS-Messagetype").get(0))) {

				return new ResponseEntity<String>("MessageProcessedNotificationMessage",
						MessageUtil.REMOVE_IDS_MESSAGE_HEADERS(resp.getHeaders()), HttpStatus.OK);
			}
			return new ResponseEntity<String>(resp.getBody(), MessageUtil.REMOVE_IDS_MESSAGE_HEADERS(resp.getHeaders()),
					HttpStatus.OK);
		}

		return resp;
	}

	private Message createRequestMessage(ProxyRequest proxyRequest) {
		String messageType = proxyRequest.getMessageType();
		if (ArtifactRequestMessage.class.getSimpleName().equals(messageType)) {
			if (StringUtils.isNoneBlank(proxyRequest.getTransferContract())) {
				logger.info("Creating ArtifactRequest message transfer contract from request");
				return getArtifactRequestMessageWithTransferContract(proxyRequest.getRequestedArtifact(),
						proxyRequest.getTransferContract());
			} else {
				logger.info("Creating ArtifactRequest message with default transfer contract");
				String artifactURI = StringUtils.isNoneBlank(proxyRequest.getRequestedArtifact())
						? proxyRequest.getRequestedArtifact()
						: UtilMessageService.REQUESTED_ARTIFACT.toString();
				return getArtifactRequestMessageWithTransferContract(artifactURI,
						UtilMessageService.TRANSFER_CONTRACT.toString());
			}

		} else if (ContractAgreementMessage.class.getSimpleName().equals(messageType)) {
			return new ContractAgreementMessageBuilder()._modelVersion_(UtilMessageService.MODEL_VERSION)
//						._transferContract_(URI.create("http://transferedContract"))
					._correlationMessage_(URI.create("http://correlationMessage"))._issued_(UtilMessageService.ISSUED)
					._issuerConnector_(URI.create(issueConnector))
					._securityToken_(UtilMessageService.getDynamicAttributeToken())
					._senderAgent_(UtilMessageService.SENDER_AGENT).build();
		} else if (ContractRequestMessage.class.getSimpleName().equals(messageType)) {
			return new ContractRequestMessageBuilder()._issued_(UtilMessageService.ISSUED)
					._modelVersion_(UtilMessageService.MODEL_VERSION)._issuerConnector_(URI.create(issueConnector))
					._recipientConnector_(Util.asList(URI.create(proxyRequest.getForwardTo())))
					._senderAgent_(UtilMessageService.SENDER_AGENT)
					._securityToken_(UtilMessageService.getDynamicAttributeToken()).build();
		} else if (DescriptionRequestMessage.class.getSimpleName().equals(messageType)) {
			URI reqEl = proxyRequest.getRequestedElement() == null ? null
					: URI.create(proxyRequest.getRequestedElement());
			return new DescriptionRequestMessageBuilder()._issued_(UtilMessageService.ISSUED)
					._issuerConnector_(URI.create(issueConnector))._modelVersion_(UtilMessageService.MODEL_VERSION)
					._requestedElement_(reqEl)._senderAgent_(UtilMessageService.SENDER_AGENT)
					._securityToken_(UtilMessageService.getDynamicAttributeToken()).build();
		}
		return null;
	}

	private Message getArtifactRequestMessageWithTransferContract(String requestedArtifact, String transferContract) {
		return new ArtifactRequestMessageBuilder()._issued_(UtilMessageService.ISSUED)
				._transferContract_(URI.create(transferContract))._issuerConnector_(URI.create(issueConnector))
				._modelVersion_(UtilMessageService.MODEL_VERSION)._requestedArtifact_(URI.create(requestedArtifact))
				._senderAgent_(UtilMessageService.SENDER_AGENT)
				._securityToken_(UtilMessageService.getDynamicAttributeToken()).build();
	}

	private HttpHeaders createMessageAsHeader(ProxyRequest proxyRequest) {
		String messageType = proxyRequest.getMessageType();
		HttpHeaders httpHeaders = new HttpHeaders();
		if (ArtifactRequestMessage.class.getSimpleName().equals(messageType)) {
			httpHeaders.add("IDS-Messagetype", "ids:" + ArtifactRequestMessage.class.getSimpleName());
			httpHeaders.add("IDS-Id", "https://w3id.org/idsa/autogen/" + ArtifactRequestMessage.class.getSimpleName()
					+ "/" + UUID.randomUUID());
			httpHeaders.add("IDS-RequestedArtifact",
					proxyRequest.getRequestedArtifact() != null ? proxyRequest.getRequestedArtifact()
							: UtilMessageService.REQUESTED_ARTIFACT.toString());
		} else if (ContractRequestMessage.class.getSimpleName().equals(messageType)) {
			httpHeaders.add("IDS-Messagetype", "ids:" + ContractRequestMessage.class.getSimpleName());
			httpHeaders.add("IDS-Id", "https://w3id.org/idsa/autogen/" + ContractRequestMessage.class.getSimpleName()
					+ "/" + UUID.randomUUID());
		} else if (ContractAgreementMessage.class.getSimpleName().equals(messageType)) {
			httpHeaders.add("IDS-Messagetype", "ids:" + ContractAgreementMessage.class.getSimpleName());
			httpHeaders.add("IDS-Id", "https://w3id.org/idsa/autogen/" + ContractAgreementMessage.class.getSimpleName()
					+ "/" + UUID.randomUUID());
		} else if (DescriptionRequestMessage.class.getSimpleName().equals(messageType)) {
			httpHeaders.add("IDS-Messagetype", "ids:" + DescriptionRequestMessage.class.getSimpleName());
			httpHeaders.add("IDS-Id", "https://w3id.org/idsa/autogen/" + DescriptionRequestMessage.class.getSimpleName()
					+ "/" + UUID.randomUUID());
			httpHeaders.add("IDS-RequestedElement", proxyRequest.getRequestedElement());
		}

		httpHeaders.add("IDS-ModelVersion", UtilMessageService.MODEL_VERSION);
		if (StringUtils.isNoneBlank(proxyRequest.getTransferContract())) {
			httpHeaders.add("IDS-TransferContract",
					proxyRequest.getTransferContract() != null ? proxyRequest.getTransferContract()
							: UtilMessageService.TRANSFER_CONTRACT.toString());
		}
		httpHeaders.add("IDS-Issued", DateUtil.now().toXMLFormat());
		httpHeaders.add("IDS-IssuerConnector", issueConnector);
		httpHeaders.add("IDS-SenderAgent", issueConnector);
		httpHeaders.add("IDS-CorrelationMessage", "http://correlationMessage");

		httpHeaders.add("IDS-SecurityToken-Type", "ids:DynamicAttributeToken");
		httpHeaders.add("IDS-SecurityToken-Id", "https://w3id.org/idsa/autogen/" + UUID.randomUUID());
		httpHeaders.add("IDS-SecurityToken-TokenFormat", TokenFormat.JWT.getId().toString());
		httpHeaders.add("IDS-SecurityToken-TokenValue", UtilMessageService.TOKEN_VALUE);

		return httpHeaders;
	}

	private void logHttpHeadersResponse(ResponseEntity<String> resp) {
		logger.info("Response received with status code {}", resp.getStatusCode());
		logger.info("Response message is of type {}", resp.getHeaders().get("IDS-Messagetype").get(0));
	}

	private void logMultipartResponse(ResponseEntity<String> resp, MultipartMessage mm) {
		logger.info("Response received with status code {}", resp.getStatusCode());
		logger.info("Response message is of type {}", MessageUtil.getIDSMessageType(mm.getHeaderContent()));
	}

	// TODO should we move this method to separate class?
	private String saveFileToDisk(String responseMessage, Message requestMessage) throws IOException {
		MultipartMessage response = MultipartMessageProcessor.parseMultipartMessage(responseMessage);
		Message responseMsg = response.getHeaderContent();

		String requestedArtifact = null;
		if (requestMessage instanceof ArtifactRequestMessage && responseMsg instanceof ArtifactResponseMessage) {
			String payload = response.getPayloadContent();
			String reqArtifact = ((ArtifactRequestMessage) requestMessage).getRequestedArtifact().getPath();
			// get resource from URI http://w3id.org/engrd/connector/artifact/ +
			// requestedArtifact
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

	private String downloadAndSaveFile(String responseMessage, Message requestMessage) throws IOException {
		MultipartMessage response = MultipartMessageProcessor.parseMultipartMessage(responseMessage);
		Message responseMsg = response.getHeaderContent();
		String requestedArtifact = null;
		if (requestMessage instanceof ArtifactRequestMessage && responseMsg instanceof ArtifactResponseMessage) {
			String payload = response.getPayloadContent();
			String reqArtifact = ((ArtifactRequestMessage) requestMessage).getRequestedArtifact().getPath();
			requestedArtifact = reqArtifact.substring(reqArtifact.lastIndexOf('/') + 1);

			try {
				if (ftpClient.downloadArtifact(requestedArtifact)) {
					if (verifyCheckSum) {
						String payloadCheckSum = getChecksum(payload);
						String downloadedChecksum = checkSumService.get().calculateCheckSumToString(requestedArtifact,
								requestMessage);
						if (StringUtils.equals(payloadCheckSum, downloadedChecksum)) {
							logger.info("File downloaded and saved");
						} else {
							logger.error("Downloaded file corrupted, deleteing it...");
							String requestedArttifactName = dataLakeDirectory + FileSystems.getDefault().getSeparator()
									+ requestedArtifact;
							File corruptedFile = new File(requestedArttifactName);
							if (corruptedFile.delete()) {
								logger.info("File deleted successfully.");
								requestedArtifact = null;
							} else {
								logger.error("Failed to delete the file.");
								requestedArtifact = null;
							}
						}
					} else {
						logger.info("File downloaded and saved");
					}
				}
			} catch (Exception e) {
				logger.error("Error while processing request {}", e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Error while processing request, check logs for more details", e);
			}
		} else {
			logger.info("Did not have ArtifactRequestMessage and ResponseMessage - nothing to save");
			requestedArtifact = null;
		}

		return requestedArtifact;
	}

	@Override
	public ResponseEntity<String> proxyWSSRequest(ProxyRequest proxyRequest) throws IOException {
		String forwardToInternal = proxyRequest.getForwardToInternal();
		String forwardTo = proxyRequest.getForwardTo();
		Message requestMessage = null;
		logger.info("Proxying wss request ...");
		if (StringUtils.isEmpty(forwardTo) || StringUtils.isEmpty(forwardToInternal)) {
			return ResponseEntity.badRequest().body("Missing required fields Forward-To or Forward-To-Internal");
		}

		FileRecreatorBeanExecutor.getInstance().setForwardTo(forwardTo);
		String responseMessage = null;
		try {
			requestMessage = createRequestMessage(proxyRequest);
			responseMessage = WebSocketClientManager.getMessageWebSocketSender().sendMultipartMessageWebSocketOverHttps(
					UtilMessageService.getMessageAsString(requestMessage), proxyRequest.getPayload(),
					forwardToInternal);
		} catch (Exception exc) {
			logger.error("Error while processing request {}", exc);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Error while processing request, check logs for more details", exc);
		}
		MultipartMessage mm = MultipartMessageProcessor.parseMultipartMessage(responseMessage);

		if (mm.getHeaderContent() instanceof ArtifactResponseMessage) {
			try {
				String fileNameSaved;
				if (isSftp(proxyRequest.getPayload())) {
					fileNameSaved = downloadAndSaveFile(responseMessage, requestMessage);
				} else {
					fileNameSaved = saveFileToDisk(responseMessage, requestMessage);
				}
				if (fileNameSaved != null) {
					return ResponseEntity.ok("{​​\"message\":\"File '" + fileNameSaved + "' created successfully\"}");
				}
				return ResponseEntity.ok(responseMessage);
			} catch (IOException e) {
				logger.error("Error while processing request {}", e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Error while processing request, check logs for more details", e);
			}

		} else {
			return handleResponse(null, mm, proxyRequest);
		}
	}

	private ResponseEntity<String> sendMultipartRequest(URI thirdPartyApi, HttpEntity<?> requestEntity,
			ProxyRequest proxyRequest) {

		try {
			ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity,
					String.class);
			MultipartMessage mm = MultipartMessageProcessor.parseMultipartMessage(resp.getBody());
			logMultipartResponse(resp, mm);

			if (mm.getHeaderContent() instanceof ArtifactResponseMessage && verifyCheckSum) {
				return handleResponse(resp, mm, proxyRequest);
			} else if (mm.getHeaderContent() instanceof DescriptionResponseMessage && verifyCheckSum) {
				String payloadContent = mm.getPayloadContent();
				if (proxyRequest.getRequestedElement() != null) {
					Resource resource = new Serializer().deserialize(payloadContent, Resource.class);

					storeCheckSum(resource, proxyRequest.getRequestedElement());
				} else {
					return handleResponse(resp, mm, proxyRequest);
				}
			}
			return handleResponse(resp, mm, proxyRequest);
		} catch (RestClientException | IOException e) {
			logger.error("Following error occured: {}", e);
			return new ResponseEntity<String>("Message could not be processed", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void storeCheckSum(Resource resource, String requestedArtifact) {
		resource.getRepresentation().forEach(representation -> {
			representation.getInstance().forEach(instance -> {

				ArtifactImpl artifact = null;
				try {
					artifact = new Serializer().deserialize(instance.toString(), ArtifactImpl.class);
				} catch (IOException e) {
					logger.error("Following error occured: {}", e);
					throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
							"Error while processing request, check logs for more details", e);
				}
				String artifactId = artifact.getId().toString();

				if (artifact.getCheckSum() != null) {
					Long checkSum = Long.parseLong(artifact.getCheckSum());
					checkSumService.ifPresent(service -> service.addCheckSum(artifactId, checkSum));
					logger.info("CheckSum stored");
				} else {
					checkSumService.ifPresent(service -> service.addCheckSum(artifactId, 0L));
					logger.info("Artifact doesn't have checkSum");
				}

			});
		});
	}

	private ResponseEntity<String> handleResponse(ResponseEntity<String> resp, MultipartMessage mm,
			ProxyRequest proxyRequest) throws IOException {
		if (mm.getHeaderContent() instanceof RejectionMessage) {
			if (((RejectionMessage) mm.getHeaderContent()).getRejectionReason() == null) {
				return new ResponseEntity<String>("Error while processing message", HttpStatus.BAD_REQUEST);
			}
			return RejectionUtil.HANDLE_REJECTION(((RejectionMessage) mm.getHeaderContent()).getRejectionReason());
		}

		String responsePayload = null;

		if (mm.getHeaderContent() instanceof ArtifactResponseMessage && verifyCheckSum) {

			if (encodePayload) {
				responsePayload = new String(Base64.getDecoder().decode(mm.getPayloadContent()));
			} else {
				responsePayload = mm.getPayloadContent();
			}
			Long storedCheckSum = checkSumService
					.map(service -> service.getCheckSumByArtifactId(proxyRequest.getRequestedArtifact())).orElse(null);

			if (storedCheckSum != null) {
				if (storedCheckSum.equals(0L)) {
					logger.info("Artifact doesn't have checksum, skipping verification");
				} else {
					logger.info("Verifying checkSum...");
					Long payloadCheckSum = calculatePayloadCheckSum(responsePayload);
					if (!payloadCheckSum.equals(storedCheckSum)) {
						logger.error("File integrity has been broken, check sums are different");
						return new ResponseEntity<String>("File integrity has been broken, check sums are different",
								HttpStatus.BAD_REQUEST);
					}
					logger.info("CheckSum verified");
				}
			} else {
				try {

					Long artifactCheckSum = fetchChecksum(proxyRequest.getForwardTo(),
							proxyRequest.getRequestedArtifact());

					if (artifactCheckSum.equals(0L)) {
						logger.info("Artifact doesn't have checksum, skipping verification");

					} else {
						Long payloadCheckSum = calculatePayloadCheckSum(responsePayload);

						if (!payloadCheckSum.equals(artifactCheckSum)) {
							logger.error("File integrity has been broken, check sums are different");
							return new ResponseEntity<String>(
									"File integrity has been broken, check sums are different", HttpStatus.BAD_REQUEST);
						}
						logger.info("CheckSum verified");
					}
				} catch (URISyntaxException e) {
					logger.error("Error while processing request {}", e);
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
							"Error while deseriliazing object, check logs for more details", e);
				}
			}
		}

		if (mm.getHeaderContent() instanceof DescriptionResponseMessage
				&& ObjectUtils.isEmpty(proxyRequest.getRequestedElement())) {
			boolean selfDescriptionValid = selfDescriptionValidator.validateSelfDescription(mm.getPayloadContent());
			if (!selfDescriptionValid) {
				return new ResponseEntity<String>("Invalid self description received - check logs for more details",
						HttpStatus.BAD_REQUEST);
			}
		}
		HttpHeaders httpHeaders = new HttpHeaders();
		if (resp != null) {
			httpHeaders.putAll(resp.getHeaders());
		}

		httpHeaders.remove(HTTP.TRANSFER_ENCODING);
		if (extractPayloadFromResponse) {
			String payloadContentType = ContentType.TEXT_PLAIN.getMimeType();
			if (StringUtils.isNotBlank(mm.getPayloadContent())) {
				logger.info("Extracting payload");
				responsePayload = mm.getPayloadContent();

				if (encodePayload && mm.getHeaderContent() instanceof ArtifactResponseMessage) {
					responsePayload = new String(Base64.getDecoder().decode(mm.getPayloadContent()));
				}
				if (mm.getPayloadHeader().get(HTTP.CONTENT_TYPE) != null) {

					payloadContentType = mm.getPayloadHeader().get(HTTP.CONTENT_TYPE);
				}
			} else {
				logger.info("Payload is null or empty - returning message");
				// payload null or empty
				responsePayload = mm.getHeaderContent().getClass().getSimpleName().replace("Impl", "");
			}
			httpHeaders.put(HTTP.CONTENT_TYPE, List.of(payloadContentType));
			httpHeaders.put(HTTP.CONTENT_LEN, List.of(String.valueOf(responsePayload.length())));
			return ResponseEntity.status(HttpStatus.OK).headers(httpHeaders).body(responsePayload);
		}

		return resp;
	}

	private Long calculatePayloadCheckSum(String responsePayload) {
		return checkSumService.map(service -> service.calculateCheckSum(responsePayload.getBytes())).orElse(null);
	}

	private boolean isSftp(Object payload) {
		logger.info("Checking the type of WSS flow...");

		if (payload == null || ObjectUtils.isEmpty(payload)) {
			logger.info("Payload is empty, saving file directly from payload...");
			return false;
		} else {
			String payloadString = payload.toString();
			try {
				JSONObject jsonObject = (JSONObject) parser.parse(payloadString);

				if (jsonObject.containsKey("sftp") && (Boolean) jsonObject.get("sftp")) {
					logger.info("Proceeding with downloading through SFTP...");
					return true;
				} else {
					logger.error(
							"Either 'sftp' is not present, or its value is not true, trying to save file directly from payload...");
					return false;
				}
			} catch (ParseException e) {
				logger.error("Could not serialize payload.", e);

				throw new InternalRecipientException("Could not serialize payload");
			}
		}
	}

	private String getChecksum(Object payload) {
		String payloadString = payload.toString();
		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) parser.parse(payloadString);
			if (jsonObject.containsKey("checkSum")) {
				logger.info("Extracting checkSum from payload...");

				return jsonObject.get("checkSum").toString();
			} else {
				logger.error("Payload doesn't contain checkSum");
				return null;
			}
		} catch (ParseException e) {
			logger.error("Could not serialize payload.", e);

			throw new InternalRecipientException("Could not serialize payload");
		}
	}
}

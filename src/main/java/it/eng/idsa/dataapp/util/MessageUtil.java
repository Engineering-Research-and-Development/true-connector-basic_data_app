package it.eng.idsa.dataapp.util;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessageBuilder;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ContractAgreementBuilder;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessageBuilder;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractRequest;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.DescriptionResponseMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
import de.fraunhofer.iais.eis.NotificationMessageBuilder;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ResultMessageBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

@Component
public class MessageUtil {
	private static final Logger logger = LoggerFactory.getLogger(MessageUtil.class);
	
	private RestTemplate restTemplate;
	private ECCProperties eccProperties;
	private Boolean encodePayload;
	private Boolean contractNegotiationDemo;
	private String issueConnector;
	private String usageControlVersion;
	private Path dataLakeDirectory;
	
	private static Serializer serializer;
	static {
		serializer = new Serializer();
	}
	
	public MessageUtil(RestTemplate restTemplate, 
			ECCProperties eccProperties,
			@Value("#{new Boolean('${application.encodePayload:false}')}") Boolean encodePayload,
			@Value("${application.contract.negotiation.demo}") Boolean contractNegotiationDemo,
			@Value("${application.ecc.issuer.connector}") String issuerConnector,
			@Value("${application.usageControlVersion}") String usageControlVersion,
			@Value("${application.dataLakeDirectory}") Path dataLakeDirectory) {
		super();
		this.restTemplate = restTemplate;
		this.eccProperties = eccProperties;
		this.encodePayload = encodePayload;
		this.contractNegotiationDemo = contractNegotiationDemo;
		this.issueConnector = issuerConnector;
		this.usageControlVersion = usageControlVersion;
		this.dataLakeDirectory = dataLakeDirectory;
	}
	
	public String createResponsePayload(Message requestHeader, String payload) {
		if (requestHeader instanceof ContractRequestMessage) {
			if (contractNegotiationDemo) {
				logger.info("Returning default contract agreement");
				if (StringUtils.equals("platoon", usageControlVersion)) {
					return createContractAgreementPlatoon(requestHeader.getIssuerConnector(), payload);
				}
				if (StringUtils.equals("mydata", usageControlVersion)) {
					return createContractAgreementMyData();
				}
			} else {
				logger.info("Creating processed notification, contract agreement needs evaluation");
				try {
					return MultipartMessageProcessor.serializeToJsonLD(createProcessNotificationMessage(requestHeader));
				} catch (IOException e) {
					logger.error("Error while creating message", e);
				}
				return null;
			}
		} else if (requestHeader instanceof ContractAgreementMessage) {
			return null;
		} else if (requestHeader instanceof DescriptionRequestMessage) {
			DescriptionRequestMessage drm = (DescriptionRequestMessage) requestHeader;
			if (drm.getRequestedElement() != null) {
				String element = getRequestedElement(drm.getRequestedElement(), getSelfDescription());
				if (StringUtils.isNotBlank(element)) {
					return element;
				} else {
					try {
						return MultipartMessageProcessor.serializeToJsonLD(createRejectionCommunicationLocalIssues(drm));
					} catch (IOException e) {
						logger.error("Could not serialize rejection", e);
					}
					return null;
				}
			} else {
				return getSelfDescriptionAsString();
			}
		} else if (requestHeader instanceof ArtifactRequestMessage && isBigPayload(((ArtifactRequestMessage) requestHeader).getRequestedArtifact().toString())) {
			return encodePayload == true ? encodePayload(BigPayload.BIG_PAYLOAD.getBytes()) : BigPayload.BIG_PAYLOAD;
		}
			return  encodePayload == true ? encodePayload(createResponsePayload().getBytes()) : createResponsePayload();
	}
	
	private boolean isBigPayload(String path) {
		String isBig = path.substring(path.lastIndexOf('/'));
		if (isBig.equals("/big")) {
			return true;
		}
		return false;
	}

	private String createContractAgreementMyData() {
	        String contractAgreement = null;
	        byte[] bytes;
	        try {
	            bytes = Files.readAllBytes(dataLakeDirectory.resolve("contract_agreement.json"));
	            contractAgreement = IOUtils.toString(bytes, "UTF8");
	        } catch (IOException e) {
				logger.error("Error while reading contract agreement file from dataLakeDirectory {}", e);
			}
			return contractAgreement;
	            
	}

	public String createResponsePayload(HttpHeaders httpHeaders, String payload) {
		String requestMessageType = httpHeaders.getFirst("IDS-Messagetype");
		if (requestMessageType.contains(ContractRequestMessage.class.getSimpleName())) {
			//header message is not used (at the moment) so we can pass null here for first parameter
			return createContractAgreementPlatoon(URI.create(httpHeaders.get("IDS-IssuerConnector").get(0)), payload);
		} else if (requestMessageType.contains(ContractAgreementMessage.class.getSimpleName())) {
			return null;
		} else if (requestMessageType.contains(DescriptionRequestMessage.class.getSimpleName())) {
			if (httpHeaders.containsKey("IDS-RequestedElement")) {
				String element = getRequestedElement(URI.create(httpHeaders.get("IDS-RequestedElement").get(0)), getSelfDescription());
				if (StringUtils.isNotBlank(element)) {
					return element;
				} else {
					return "IDS-RejectionReason:" + RejectionReason.NOT_FOUND.getId().toString();
				}
			} else {
				return getSelfDescriptionAsString();
			}
		} else if (requestMessageType.contains(ArtifactRequestMessage.class.getSimpleName()) && isBigPayload(httpHeaders.getFirst("IDS-RequestedArtifact"))) {
			return encodePayload == true ? encodePayload(BigPayload.BIG_PAYLOAD.getBytes()) : BigPayload.BIG_PAYLOAD;
		} else {
			return  encodePayload == true ? encodePayload(createResponsePayload().getBytes()) : createResponsePayload();
		}
	}
	

	private String createResponsePayload() {
		// Put check sum in the payload
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String formattedDate = dateFormat.format(date);

		Map<String, String> jsonObject = new HashMap<>();
		jsonObject.put("firstName", "John");
		jsonObject.put("lastName", "Doe");
		jsonObject.put("dateOfBirth", formattedDate);
		jsonObject.put("address", "591  Franklin Street, Pennsylvania");
		jsonObject.put("checksum", "ABC123 " + formattedDate);
		Gson gson = new GsonBuilder().create();
		return gson.toJson(jsonObject);
	}

	private String encodePayload(byte[] payload) {
		logger.info("Encoding payload");
		return Base64.getEncoder().encodeToString(payload);
	}
	
	private String createContractAgreementPlatoon(URI consumerURI, String payload) {
		try {
			Connector connector = getSelfDescription();
			ContractRequest contractRequest = serializer.deserialize(payload, ContractRequest.class);

			ContractOffer co = getPermissionAndTarget(connector, 
					contractRequest.getPermission().get(0).getId(), 
					contractRequest.getPermission().get(0).getTarget());
			List<Permission> permissions = new ArrayList<>();
			if(co == null) {
				logger.info("Could not find contract offer that match with request - permissionId and target");
				return null;
			}
			for(Permission p: co.getPermission()) {
				if(p.getId().equals(contractRequest.getPermission().get(0).getId()) 
						&& p.getTarget().equals(contractRequest.getPermission().get(0).getTarget())) {
					permissions.add(p);
				}
			}
			ContractAgreement ca = new ContractAgreementBuilder()
					._permission_(permissions)
					._contractStart_(co.getContractStart())
					._contractDate_(co.getContractDate())
					._consumer_(consumerURI)
					._provider_(URI.create(issueConnector))
					.build();
		
			return MultipartMessageProcessor.serializeToJsonLD(ca);
		} catch (IOException e) {
			logger.error("Error while creating contract agreement", e);
		}
		return null;
	}
	
	private ContractOffer getPermissionAndTarget(Connector connector, URI permission, URI target) {
		for (ResourceCatalog resourceCatalog : connector.getResourceCatalog()) {
			for (Resource resource : resourceCatalog.getOfferedResource()) {
				for (ContractOffer co : resource.getContractOffer()) {
					for (Permission p : co.getPermission()) {
						if (p.getId().equals(permission) && p.getTarget().equals(target)) {
							logger.info("Found permission");
							return co;
						}
					}
				}
			}
		}
		return null;
	}
	
	private Connector getSelfDescription() {
		URI eccURI = null;

		try {
			eccURI = new URI(eccProperties.getRESTprotocol(), null, eccProperties.getHost(), eccProperties.getRESTport(), null, null, null);
			logger.info("Fetching self description from ECC {}.", eccURI.toString());
			String selfDescription = restTemplate.getForObject(eccURI, String.class);
			logger.info("Deserializing self description.");
			logger.debug("Self description content: {}{}", System.lineSeparator(), selfDescription);
			return serializer.deserialize(selfDescription, Connector.class);
		} catch (URISyntaxException e) {
			logger.error("Could not create URI for Self Description request.", e);
			return null;
		} catch (RestClientException e) {
			logger.error("Could not fetch self description from ECC", e);
			return null;
		} catch (IOException e) {
			logger.error("Could not deserialize self description to Connector instance", e);
			return null;
		}
	}
	
	private String getSelfDescriptionAsString() {
		try {
			return MultipartMessageProcessor.serializeToJsonLD(getSelfDescription());
		} catch (IOException e) {
			logger.error("Could not serialize self description", e);
		}
		return null;
	}
	
	private String getRequestedElement(URI requestedElement, Connector connector) {
		for (ResourceCatalog catalog : connector.getResourceCatalog()) {
			for (Resource offeredResource : catalog.getOfferedResource()) {
				if (requestedElement.equals(offeredResource.getId())) {
					try {
						return MultipartMessageProcessor.serializeToJsonLD(offeredResource);
					} catch (IOException e) {
						logger.error("Could not serialize requested element.", e);
					}
				}
			}
		}
		logger.error("Requested element not found.");
		return null;
	}
	
	public Message getResponseHeader(Message header) {
		Message output = null;
		if (null == header || null == header.getId() || header.getId().toString().isEmpty())
			header = new NotificationMessageBuilder()
			._securityToken_(UtilMessageService.getDynamicAttributeToken())
			._senderAgent_(whoIAmEngRDProvider())
			.build();
		if (header instanceof ArtifactRequestMessage) {
			output = createArtifactResponseMessage((ArtifactRequestMessage) header);
		} else if (header instanceof ContractRequestMessage) {
			if(contractNegotiationDemo) {
				logger.info("Returning default contract agreement");
				output = createContractAgreementMessage((ContractRequestMessage) header);
			} else {
				logger.info("Creating processed notification, contract agreement needs evaluation");
				output= createProcessNotificationMessage(null);
			}
		} else if (header instanceof ContractAgreementMessage) {
			output = createProcessNotificationMessage(header);
		} else if (header instanceof DescriptionRequestMessage) {
			output = createDescriptionResponseMessage((DescriptionRequestMessage) header);
		} else {
			output = createResultMessage(header);
		}
		return output;
	}
	
	public Message createResultMessage(Message header) {
		return new ResultMessageBuilder()
				._issuerConnector_(whoIAmEngRDProvider())
				._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._senderAgent_(whoIAmEngRDProvider())
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._securityToken_(UtilMessageService.getDynamicAttributeToken())
				.build();
	}

	public Message createArtifactResponseMessage(ArtifactRequestMessage header) {
		// Need to set transferCotract from original message, it will be used in policy enforcement
		return new ArtifactResponseMessageBuilder()
				._issuerConnector_(whoIAmEngRDProvider())
				._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._transferContract_(header.getTransferContract())
				._senderAgent_(whoIAmEngRDProvider())
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._securityToken_(UtilMessageService.getDynamicAttributeToken())
				.build();
	}
	
	public Message createContractAgreementMessage(ContractRequestMessage header) {
		return new ContractAgreementMessageBuilder()
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._transferContract_(header.getTransferContract())
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._issued_(DateUtil.now())
				._issuerConnector_(whoIAmEngRDProvider())
				._senderAgent_(whoIAmEngRDProvider())
				._recipientConnector_(Util.asList(header != null ? header.getIssuerConnector() : whoIAm()))
				._securityToken_(UtilMessageService.getDynamicAttributeToken())
				._senderAgent_(whoIAmEngRDProvider())
				.build();
	}
	
	private Message createDescriptionResponseMessage(DescriptionRequestMessage header) {
		return new DescriptionResponseMessageBuilder()
				._issuerConnector_(whoIAmEngRDProvider())
				._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._securityToken_(UtilMessageService.getDynamicAttributeToken())
				._senderAgent_(whoIAmEngRDProvider())
				.build();
	}

	public Message createRejectionMessage(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(whoIAmEngRDProvider())
				._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._rejectionReason_(RejectionReason.MALFORMED_MESSAGE)
				._securityToken_(UtilMessageService.getDynamicAttributeToken())
				._senderAgent_(whoIAmEngRDProvider())
				.build();
	}

	public Message createRejectionToken(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(whoIAmEngRDProvider())
				._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._rejectionReason_(RejectionReason.NOT_AUTHENTICATED)
				._securityToken_(UtilMessageService.getDynamicAttributeToken())
				._senderAgent_(whoIAmEngRDProvider())
				.build();
	}

	private URI whoIAm() {
		return URI.create("http://auto-generated");
	}
	
	private URI whoIAmEngRDProvider() {
		return URI.create(issueConnector);
	}
	
	private Message createProcessNotificationMessage(Message header) {
		return new MessageProcessedNotificationMessageBuilder()
				._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._issuerConnector_(whoIAmEngRDProvider())
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._securityToken_(UtilMessageService.getDynamicAttributeToken())
				._senderAgent_(whoIAmEngRDProvider())
				.build();
	}	

	public Message createRejectionCommunicationLocalIssues(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(whoIAmEngRDProvider())
				._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._rejectionReason_(RejectionReason.NOT_FOUND)
				._securityToken_(UtilMessageService.getDynamicAttributeToken())
				._senderAgent_(whoIAmEngRDProvider())
				.build();
	}
	
	/**
	 * 
	 * @param header
	 * @param payload
	 * @param payloadContentType if is null, using default - application/json, otherwise using the one that is passed as in param
	 * @return
	 */
	public HttpEntity createMultipartMessageForm(String header, String payload, ContentType payloadContentType) {
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create()
				.setStrictMode();
		
		ContentType payloadCT =  ContentType.TEXT_PLAIN;
		
		if (payloadContentType == null) {
			if (isValidJSON(payload)) {
				payloadCT = ContentType.APPLICATION_JSON;
			}
		} else {
			payloadCT = payloadContentType;
		}
		
		try {
			FormBodyPart bodyHeaderPart;
			ContentBody headerBody = new StringBody(header, ContentType.create("application/ld+json"));
			bodyHeaderPart = FormBodyPartBuilder.create("header", headerBody).build();
			bodyHeaderPart.addField(HTTP.CONTENT_LEN, "" + header.length());
			multipartEntityBuilder.addPart(bodyHeaderPart);

			FormBodyPart bodyPayloadPart = null;
			if (payload != null) {
				ContentBody payloadBody = new StringBody(payload, payloadCT);
				bodyPayloadPart = FormBodyPartBuilder.create("payload", payloadBody).build();
				bodyPayloadPart.addField(HTTP.CONTENT_LEN, "" + payload.length());
				multipartEntityBuilder.addPart(bodyPayloadPart);
			}

		} catch (Exception e) {
			logger.error("Error while creating response ", e);
		}
		return multipartEntityBuilder.build();
	}
	
	public boolean isValidJSON(String json) {
	    try {
	        JsonParser.parseString(json);
	    } catch (JsonSyntaxException e) {
	        return false;
	    }
	    return true;
	}

	public static MultiValueMap<String, String> REMOVE_IDS_MESSAGE_HEADERS(HttpHeaders headers) {
		MultiValueMap<String, String> newHeaders = new LinkedMultiValueMap<>();
		newHeaders.putAll(headers);
		for (Iterator<String> iterator = newHeaders.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			//String.contains is case sensitive so this should have minimal margin of error
			if (key.contains("IDS-")) {
				iterator.remove();
			}
		}
		return newHeaders;
	}
}

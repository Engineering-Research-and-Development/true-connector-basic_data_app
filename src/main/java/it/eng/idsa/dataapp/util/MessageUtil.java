package it.eng.idsa.dataapp.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Component
public class MessageUtil {
	private static final Logger logger = LoggerFactory.getLogger(MessageUtil.class);
	
	private Path dataLakeDirectory;
	
	private RestTemplate restTemplate;
	
	private ECCProperties eccProperties;
	
	private MultiPartMessageService multiPartMessageService;
	
	
	
	public MessageUtil(@Value("${application.dataLakeDirectory}") Path dataLakeDirectory, RestTemplate restTemplate, ECCProperties eccProperties, MultiPartMessageService multiPartMessageService) {
		super();
		this.dataLakeDirectory = dataLakeDirectory;
		this.restTemplate = restTemplate;
		this.eccProperties = eccProperties;
		this.multiPartMessageService = multiPartMessageService;
	}

	public String createResponsePayload(Message requestHeader) {
		if (requestHeader instanceof ContractRequestMessage) {
			return createContractAgreement(dataLakeDirectory);
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
						return MultipartMessageProcessor.serializeToJsonLD(multiPartMessageService.createRejectionCommunicationLocalIssues(drm));
					} catch (IOException e) {
						logger.error("Could not serialize rejection", e);
					}
					return null;
				}
			} else {
				return getSelfDescriptionAsString();
			}
		} else {
			return createResponsePayload();
		}
	}
	
	public String createResponsePayload(String requestHeader) {
		if (requestHeader.contains(ContractRequestMessage.class.getSimpleName())) {
			return createContractAgreement(dataLakeDirectory);
		} else if (requestHeader.contains(ContractAgreementMessage.class.getSimpleName())) {
			return null;
		} else if (requestHeader.contains(DescriptionRequestMessage.class.getSimpleName())) {
			if (requestHeader.contains("ids:requestedElement")) {
				DescriptionRequestMessage drm = (DescriptionRequestMessage) multiPartMessageService.getMessage((Object) requestHeader);
				String element = getRequestedElement(drm.getRequestedElement(), getSelfDescription());
				if (StringUtils.isNotBlank(element)) {
					return element;
				} else {
					try {
						return MultipartMessageProcessor.serializeToJsonLD(multiPartMessageService.createRejectionCommunicationLocalIssues(drm));
					} catch (IOException e) {
						logger.error("Could not serialize rejection", e);
					}
					return null;
				}
			} else {
				return getSelfDescriptionAsString();
			}
		} else {
			return createResponsePayload();
		}
	}
	
	public String createResponsePayload(HttpHeaders httpHeaders) {
		String requestMessageType = httpHeaders.getFirst("IDS-Messagetype");
		if (requestMessageType.contains(ContractRequestMessage.class.getSimpleName())) {
			return createContractAgreement(dataLakeDirectory);
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
		} else {
			return createResponsePayload();
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
	
	private String createContractAgreement(Path dataLakeDirectory) {
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
	
	private Connector getSelfDescription() {
		URI eccURI = null;
		try {
			eccURI = new URI(eccProperties.getRESTprotocol(), null, eccProperties.getHost(), eccProperties.getRESTport(), null, null, null);
		} catch (URISyntaxException e) {
			logger.error("Could not create URI for Self Description request.", e);
		}

		try {
			return new Serializer().deserialize(restTemplate.getForObject(eccURI, String.class), Connector.class);
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
			return new Serializer().serialize(getSelfDescription());
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
}

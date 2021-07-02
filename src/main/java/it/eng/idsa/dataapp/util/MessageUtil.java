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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.fraunhofer.iais.eis.BaseConnector;
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
		if(requestHeader instanceof ContractRequestMessage) {
			return createContractAgreement(dataLakeDirectory);
		} else if(requestHeader instanceof ContractAgreementMessage) {
			return null;
		} else if(requestHeader instanceof DescriptionRequestMessage) {
			if (((DescriptionRequestMessage) requestHeader).getRequestedElement() != null) {
				return getRequestedElement(((DescriptionRequestMessage) requestHeader).getRequestedElement());
			}else {
				return getSelfDescription();
			}
		} else {
			return createResponsePayload();
		}
	}
	
	public String createResponsePayload(String requestHeader) {
		if(requestHeader.contains(ContractRequestMessage.class.getSimpleName())) {
			return createContractAgreement(dataLakeDirectory);
		} else if(requestHeader.contains(ContractAgreementMessage.class.getSimpleName())) {
			return null;
		} else if(requestHeader.contains(DescriptionRequestMessage.class.getSimpleName())) {
			if (requestHeader.contains("ids:requestedElement")) {
				return getRequestedElement(((DescriptionRequestMessage) multiPartMessageService.getMessage((Object)requestHeader)).getRequestedElement());
			}else {
				return getSelfDescription();
			}
		} else {
			return createResponsePayload();
		}
	}
	
	public String createResponsePayload(HttpHeaders httpHeaders) {
		String requestMessageType = httpHeaders.getFirst("IDS-Messagetype");
		if(requestMessageType.contains(ContractRequestMessage.class.getSimpleName())) {
			return createContractAgreement(dataLakeDirectory);
		} else if(requestMessageType.contains(ContractAgreementMessage.class.getSimpleName())) {
			return null;
		} else if(requestMessageType.contains(DescriptionRequestMessage.class.getSimpleName())) {
			if (httpHeaders.containsKey("IDS-RequestedElement")) {
				return getRequestedElement(URI.create(httpHeaders.get("IDS-RequestedElement").get(0)));
			}else {
				return getSelfDescription();
			}
		} else {
			return createResponsePayload();
		}
	}
	

	private  String createResponsePayload() {
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
	
	private String getSelfDescription() {
		URI eccURI = null;
		try {
			eccURI = new URI("http", null, eccProperties.getHost(), 
					8081, null,
					null, null);
		} catch (URISyntaxException e) {
			logger.error("Could not create URI for Self Description request.", e);
		}
		
		return restTemplate.getForObject(eccURI, String.class);
	}
	
	private String getRequestedElement(URI requestedElement) {
		BaseConnector connector = null;
		try {
			connector = new Serializer().deserialize(getSelfDescription(), BaseConnector.class);
		} catch (IOException e) {
			logger.error("Could not deserialize self description", e);
		}
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
		return "IDS-RejectionReason:" + RejectionReason.NOT_FOUND.getId().toString();
	}
}

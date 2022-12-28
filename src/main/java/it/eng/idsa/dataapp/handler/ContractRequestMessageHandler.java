package it.eng.idsa.dataapp.handler;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ContractAgreementBuilder;
import de.fraunhofer.iais.eis.ContractAgreementMessageBuilder;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractRequest;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

@Component
public class ContractRequestMessageHandler extends DataAppMessageHandler {

	private Boolean contractNegotiationDemo;
	private String usageControlVersion;
	private String issueConnector;
	private RestTemplate restTemplate;
	private ECCProperties eccProperties;
	private Path dataLakeDirectory;

	private static Serializer serializer;
	static {
		serializer = new Serializer();
	}

	private static final Logger logger = LoggerFactory.getLogger(ContractRequestMessageHandler.class);

	public ContractRequestMessageHandler(RestTemplateBuilder restTemplateBuilder, ECCProperties eccProperties,
			@Value("#{new Boolean('${application.encodePayload:false}')}") Boolean encodePayload,
			@Value("${application.contract.negotiation.demo}") Boolean contractNegotiationDemo,
			@Value("${application.ecc.issuer.connector}") String issuerConnector,
			@Value("${application.usageControlVersion}") String usageControlVersion,
			@Value("${application.dataLakeDirectory}") Path dataLakeDirectory) {
		this.restTemplate = restTemplateBuilder.build();
		this.eccProperties = eccProperties;
		this.contractNegotiationDemo = contractNegotiationDemo;
		this.issueConnector = issuerConnector;
		this.usageControlVersion = usageControlVersion;
		this.dataLakeDirectory = dataLakeDirectory;
	}

	@Override
	public Map<String, Object> handleMessage(Message message, Object payload) {
		logger.info("ContractRequestMessageHandler");

		ContractRequestMessage crm = (ContractRequestMessage) message;

		Map<String, Object> response = new HashMap<>();
		Message contractRequestResponseMessage = null;
		String responsePayload = null;

		if (contractNegotiationDemo) {
			logger.info("Returning default contract agreement");
			if (StringUtils.equals("platoon", usageControlVersion)) {
				responsePayload = createContractAgreementPlatoon(crm.getIssuerConnector(), payload.toString());
			}
			if (StringUtils.equals("mydata", usageControlVersion)) {
				responsePayload = createContractAgreementMyData();
			}
		} else {
			logger.info("Creating processed notification, contract agreement needs evaluation");
//			contractRequestResponseMessage = createProcessNotificationMessage(null);
			try {
				responsePayload = MultipartMessageProcessor
						.serializeToJsonLD(createProcessNotificationMessage(contractRequestResponseMessage));
			} catch (IOException e) {
				logger.error("Error while creating message", e);
			}
			responsePayload = null;
		}

		if (responsePayload != null) {
			contractRequestResponseMessage = createContractAgreementMessage(crm);
			response.put("header", contractRequestResponseMessage);
			response.put("payload", responsePayload);
		}

		return response;
	}

	private Message createContractAgreementMessage(ContractRequestMessage header) {
		return new ContractAgreementMessageBuilder()._modelVersion_(UtilMessageService.MODEL_VERSION)
				._transferContract_(header.getTransferContract())
				._correlationMessage_(header != null ? header.getId() : whoIAm())._issued_(DateUtil.now())
				._issuerConnector_(whoIAmEngRDProvider())._senderAgent_(whoIAmEngRDProvider())
				._recipientConnector_(Util.asList(header != null ? header.getIssuerConnector() : whoIAm()))
				._securityToken_(UtilMessageService.getDynamicAttributeToken())._senderAgent_(whoIAmEngRDProvider())
				.build();
	}

	private Message createProcessNotificationMessage(Message header) {
		return new MessageProcessedNotificationMessageBuilder()._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)._issuerConnector_(whoIAmEngRDProvider())
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._securityToken_(UtilMessageService.getDynamicAttributeToken())._senderAgent_(whoIAmEngRDProvider())
				.build();
	}

	private String createContractAgreementPlatoon(URI consumerURI, String payload) {
		try {
			Connector connector = getSelfDescription();
			ContractRequest contractRequest = serializer.deserialize(payload, ContractRequest.class);

			ContractOffer co = getPermissionAndTarget(connector, contractRequest.getPermission().get(0).getId(),
					contractRequest.getPermission().get(0).getTarget());
			List<Permission> permissions = new ArrayList<>();
			if (co == null) {
				logger.info("Could not find contract offer that match with request - permissionId and target");

				return null;
			}
			for (Permission p : co.getPermission()) {
				if (p.getId().equals(contractRequest.getPermission().get(0).getId())
						&& p.getTarget().equals(contractRequest.getPermission().get(0).getTarget())) {
					permissions.add(p);
				}
			}
			ContractAgreement ca = new ContractAgreementBuilder()._permission_(permissions)
					._contractStart_(co.getContractStart())._contractDate_(co.getContractDate())._consumer_(consumerURI)
					._provider_(URI.create(issueConnector)).build();

			return MultipartMessageProcessor.serializeToJsonLD(ca);
		} catch (IOException e) {
			logger.error("Error while creating contract agreement", e);
		}

		return null;
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

	private Connector getSelfDescription() {
		URI eccURI = null;

		try {
			eccURI = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
					eccProperties.getSelfdescriptionContext(), null, null);
			logger.info("Fetching self description from ECC {}.", eccURI.toString());

			ResponseEntity<String> response = restTemplate.exchange(eccURI, HttpMethod.GET, null, String.class);
			if (response != null) {
				if (response.getStatusCodeValue() == 200) {
					String selfDescription = response.getBody();
					logger.info("Deserializing self description.");
					logger.debug("Self description content: {}{}", System.lineSeparator(), selfDescription);

					return serializer.deserialize(selfDescription, Connector.class);
				} else {
					logger.error("Could not fetch self description, ECC responded with status {} and message \r{}",
							response.getStatusCodeValue(), response.getBody());

					return null;
				}
			}
			logger.error("Could not fetch self description, ECC did not respond");

			return null;
		} catch (URISyntaxException e) {
			logger.error("Could not create URI for Self Description request.", e);

			return null;
		} catch (IOException e) {
			logger.error("Could not deserialize self description to Connector instance", e);

			return null;
		}
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
}

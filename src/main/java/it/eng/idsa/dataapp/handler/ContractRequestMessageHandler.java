package it.eng.idsa.dataapp.handler;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.io.IOException;
import java.net.URI;
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
import org.springframework.stereotype.Component;

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
import it.eng.idsa.dataapp.service.SelfDescriptionService;
import it.eng.idsa.dataapp.web.rest.exceptions.BadParametersException;
import it.eng.idsa.dataapp.web.rest.exceptions.InternalRecipientException;
import it.eng.idsa.dataapp.web.rest.exceptions.NotFoundException;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

@Component
public class ContractRequestMessageHandler extends DataAppMessageHandler {

	private Boolean contractNegotiationDemo;
	private String usageControlVersion;
	private String issueConnector;
	private Path dataLakeDirectory;
	private SelfDescriptionService selfDescriptionService;

	private static Serializer serializer;
	static {
		serializer = new Serializer();
	}

	private static final Logger logger = LoggerFactory.getLogger(ContractRequestMessageHandler.class);

	public ContractRequestMessageHandler(SelfDescriptionService selfDescriptionService,
			@Value("#{new Boolean('${application.encodePayload:false}')}") Boolean encodePayload,
			@Value("${application.contract.negotiation.demo}") Boolean contractNegotiationDemo,
			@Value("${application.ecc.issuer.connector}") String issuerConnector,
			@Value("${application.usageControlVersion}") String usageControlVersion,
			@Value("${application.dataLakeDirectory}") Path dataLakeDirectory) {
		this.selfDescriptionService = selfDescriptionService;
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
				if (payload != null) {
					responsePayload = createContractAgreementPlatoon(crm, payload.toString());
				} else {
					throw new BadParametersException("Payload is null", message);
				}
			}
			if (StringUtils.equals("mydata", usageControlVersion)) {

				responsePayload = createContractAgreementMyData(message);
			}
		} else {
			logger.info("Creating processed notification, contract agreement needs evaluation");

			contractRequestResponseMessage = createProcessNotificationMessage(null);
			try {

				responsePayload = MultipartMessageProcessor
						.serializeToJsonLD(createProcessNotificationMessage(contractRequestResponseMessage));
			} catch (IOException e) {
				logger.error("Error while creating message", e);

				throw new InternalRecipientException(
						"Creating processed notification, contract agreement needs evaluation", message);
			}
		}
		contractRequestResponseMessage = createContractAgreementMessage(crm);

		response.put("header", contractRequestResponseMessage);
		response.put("payload", responsePayload);

		return response;
	}

	private String createContractAgreementPlatoon(Message message, String payload) {
		try {
			Connector connector = selfDescriptionService.getSelfDescription(message);
			ContractRequest contractRequest = serializer.deserialize(payload, ContractRequest.class);

			ContractOffer co = getPermissionAndTarget(connector, contractRequest.getPermission().get(0).getId(),
					contractRequest.getPermission().get(0).getTarget(), message);
			List<Permission> permissions = new ArrayList<>();
			for (Permission p : co.getPermission()) {
				if (p.getId().equals(contractRequest.getPermission().get(0).getId())
						&& p.getTarget().equals(contractRequest.getPermission().get(0).getTarget())) {
					permissions.add(p);
				}
			}
			ContractAgreement ca = new ContractAgreementBuilder()._permission_(permissions)
					._contractStart_(co.getContractStart())._contractDate_(co.getContractDate())
					._consumer_(message.getIssuerConnector())._provider_(URI.create(issueConnector)).build();

			return MultipartMessageProcessor.serializeToJsonLD(ca);
		} catch (IOException e) {
			logger.error("Error while creating contract agreement", e);

			throw new InternalRecipientException("Error while creating contract agreement", message);
		}
	}

	private String createContractAgreementMyData(Message message) {
		String contractAgreement = null;
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(dataLakeDirectory.resolve("contract_agreement.json"));
			contractAgreement = IOUtils.toString(bytes, "UTF8");
		} catch (IOException e) {
			logger.error("Error while reading contract agreement file from dataLakeDirectory {}", e);

			throw new InternalRecipientException("Error while reading contract agreement file from dataLakeDirectory",
					message);
		}

		return contractAgreement;
	}

	private ContractOffer getPermissionAndTarget(Connector connector, URI permission, URI target, Message message) {
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

		throw new NotFoundException("Could not find contract offer that match with request - permissionId and target",
				message);
	}

	private Message createContractAgreementMessage(ContractRequestMessage header) {
		return new ContractAgreementMessageBuilder()._modelVersion_(UtilMessageService.MODEL_VERSION)
				._transferContract_(header.getTransferContract())
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._issued_(DateUtil.normalizedDateTime())._issuerConnector_(whoIAmEngRDProvider())
				._senderAgent_(whoIAmEngRDProvider())
				._recipientConnector_(Util.asList(header != null ? header.getIssuerConnector() : whoIAm()))
				._securityToken_(UtilMessageService.getDynamicAttributeToken())._senderAgent_(whoIAmEngRDProvider())
				.build();
	}

	private Message createProcessNotificationMessage(Message header) {
		return new MessageProcessedNotificationMessageBuilder()._issued_(DateUtil.normalizedDateTime())
				._modelVersion_(UtilMessageService.MODEL_VERSION)._issuerConnector_(whoIAmEngRDProvider())
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._securityToken_(UtilMessageService.getDynamicAttributeToken())._senderAgent_(whoIAmEngRDProvider())
				.build();
	}
}

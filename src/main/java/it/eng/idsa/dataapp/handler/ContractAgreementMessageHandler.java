package it.eng.idsa.dataapp.handler;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

@Component
public class ContractAgreementMessageHandler extends DataAppMessageHandler {

	private static final Logger logger = LoggerFactory.getLogger(ContractAgreementMessageHandler.class);

	@Override
	public Map<String, Object> handleMessage(Message message, Object payload) {
		logger.info("Handling header through ContractAgreementMessageHandler");

		ContractAgreementMessage cam = (ContractAgreementMessage) message;
		Map<String, Object> response = new HashMap<>();
		Message contractAgreementResponseMessage = null;
		String responsePayload = null;

		contractAgreementResponseMessage = createProcessNotificationMessage(cam);

		response.put("header", contractAgreementResponseMessage);
		response.put("payload", responsePayload);

		return response;
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

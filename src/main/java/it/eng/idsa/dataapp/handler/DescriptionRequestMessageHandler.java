package it.eng.idsa.dataapp.handler;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.DescriptionResponseMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.service.SelfDescriptionService;

import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

@Component
public class DescriptionRequestMessageHandler extends DataAppMessageHandler {

	private SelfDescriptionService selfDescriptionService;

	private static final Logger logger = LoggerFactory.getLogger(DescriptionRequestMessageHandler.class);

	public DescriptionRequestMessageHandler(SelfDescriptionService selfDescriptionService) {
		this.selfDescriptionService = selfDescriptionService;
	}

	@Override
	public Map<String, Object> handleMessage(Message message, Object payload) {
		logger.info("DescriptionRequestMessage");

		DescriptionRequestMessage drm = (DescriptionRequestMessage) message;
		Map<String, Object> response = new HashMap<>();
		Message descriptionResponseMessage = null;
		String responsePayload = null;

		if (drm.getRequestedElement() != null) {
			responsePayload = selfDescriptionService.getRequestedElement(drm,
					selfDescriptionService.getSelfDescription(message));
		} else {
			responsePayload = selfDescriptionService.getSelfDescriptionAsString(message);
		}

		descriptionResponseMessage = createDescriptionResponseMessage(drm);

		response.put("header", descriptionResponseMessage);
		response.put("payload", responsePayload);

		return response;
	}

	private Message createDescriptionResponseMessage(DescriptionRequestMessage header) {
		return new DescriptionResponseMessageBuilder()._issuerConnector_(whoIAmEngRDProvider())._issued_(DateUtil.normalizedDateTime())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._securityToken_(UtilMessageService.getDynamicAttributeToken())._senderAgent_(whoIAmEngRDProvider())
				.build();
	}
}

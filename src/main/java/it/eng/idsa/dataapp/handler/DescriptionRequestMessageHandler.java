package it.eng.idsa.dataapp.handler;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.DescriptionResponseMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import it.eng.idsa.dataapp.service.SelfDescriptionService;
import it.eng.idsa.dataapp.web.rest.exceptions.InternalRecipientException;
import it.eng.idsa.dataapp.web.rest.exceptions.NotFoundException;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
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
			String element = getRequestedElement(drm.getRequestedElement(),
					selfDescriptionService.getSelfDescription(message), message);
			responsePayload = element;
		} else {
			responsePayload = selfDescriptionService.getSelfDescriptionAsString(message);
		}
		descriptionResponseMessage = createDescriptionResponseMessage(drm);

		response.put("header", descriptionResponseMessage);
		response.put("payload", responsePayload);

		return response;
	}

	private String getRequestedElement(URI requestedElement, Connector connector, Message message) {
		for (ResourceCatalog catalog : connector.getResourceCatalog()) {
			for (Resource offeredResource : catalog.getOfferedResource()) {
				if (requestedElement.equals(offeredResource.getId())) {
					try {

						return MultipartMessageProcessor.serializeToJsonLD(offeredResource);
					} catch (IOException e) {
						logger.error("Could not serialize requested element.", e);

						throw new InternalRecipientException("Could not serialize requested element", message);
					}
				}
			}
		}
		logger.error("Requested element not found.");

		throw new NotFoundException("Requested element not found", message);
	}

	private Message createDescriptionResponseMessage(DescriptionRequestMessage header) {
		return new DescriptionResponseMessageBuilder()._issuerConnector_(whoIAmEngRDProvider())._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._securityToken_(UtilMessageService.getDynamicAttributeToken())._senderAgent_(whoIAmEngRDProvider())
				.build();
	}
}

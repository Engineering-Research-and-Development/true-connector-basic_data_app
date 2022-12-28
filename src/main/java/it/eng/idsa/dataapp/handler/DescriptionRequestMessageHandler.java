package it.eng.idsa.dataapp.handler;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.DescriptionResponseMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

@Component
public class DescriptionRequestMessageHandler extends DataAppMessageHandler {

	private static Serializer serializer;
	static {
		serializer = new Serializer();
	}

	private RestTemplate restTemplate;
	private ECCProperties eccProperties;

	private static final Logger logger = LoggerFactory.getLogger(DescriptionRequestMessageHandler.class);

	public DescriptionRequestMessageHandler(RestTemplateBuilder restTemplateBuilder, ECCProperties eccProperties) {
		this.restTemplate = restTemplateBuilder.build();
		this.eccProperties = eccProperties;
	}

	@Override
	public Map<String, Object> handleMessage(Message message, Object payload) {
		logger.info("DescriptionRequestMessage");

		DescriptionRequestMessage drm = (DescriptionRequestMessage) message;

		Map<String, Object> response = new HashMap<>();

		Message descriptionResponseMessage = null;

		String responsePayload = null;
		if (drm.getRequestedElement() != null) {
			String element = getRequestedElement(drm.getRequestedElement(), getSelfDescription());
			if (StringUtils.isNotBlank(element)) {
				responsePayload = element;
			} else {
				try {
					responsePayload = MultipartMessageProcessor.serializeToJsonLD(createRejectionNotFound(drm));
				} catch (IOException e) {
					logger.error("Could not serialize rejection", e);
				}
				responsePayload = null;
			}
		} else {
			responsePayload = getSelfDescriptionAsString();
		}

		if (responsePayload != null) {
			descriptionResponseMessage = createDescriptionResponseMessage(drm);
			response.put("header", descriptionResponseMessage);
			response.put("payload", responsePayload);

		}

		return response;
	}

	private Message createDescriptionResponseMessage(DescriptionRequestMessage header) {
		return new DescriptionResponseMessageBuilder()._issuerConnector_(whoIAmEngRDProvider())._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._securityToken_(UtilMessageService.getDynamicAttributeToken())._senderAgent_(whoIAmEngRDProvider())
				.build();
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

	private String getSelfDescriptionAsString() {
		try {
			String sd = MultipartMessageProcessor.serializeToJsonLD(getSelfDescription());
			// MultipartMessageProcessor.serializeToJsonLD(getSelfDescription()) on null
			// Object returns String "null", this is a temporary fix
			// TODO fix MultipartMessageProcessor.serializeToJsonLD(getSelfDescription()) so
			// that it returns proper null if something fails
			return sd.equals("null") ? null : sd;
		} catch (IOException e) {
			logger.error("Could not serialize self description", e);
		}
		return null;
	}

	public Message createRejectionNotFound(Message header) {
		return new RejectionMessageBuilder()._issuerConnector_(whoIAmEngRDProvider())._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._rejectionReason_(RejectionReason.NOT_FOUND)
				._securityToken_(UtilMessageService.getDynamicAttributeToken())._senderAgent_(whoIAmEngRDProvider())
				.build();
	}

}

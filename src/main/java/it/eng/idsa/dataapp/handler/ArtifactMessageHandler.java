package it.eng.idsa.dataapp.handler;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.service.SelfDescriptionService;
import it.eng.idsa.dataapp.service.ThreadService;
import it.eng.idsa.dataapp.util.BigPayload;
import it.eng.idsa.dataapp.web.rest.exceptions.BadParametersException;
import it.eng.idsa.dataapp.web.rest.exceptions.NotFoundException;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

@Component
public class ArtifactMessageHandler extends DataAppMessageHandler {

	private Boolean encodePayload;
	private SelfDescriptionService selfDescriptionService;
	private ThreadService threadService;
	private Path dataLakeDirectory;
	private Boolean contractNegotiationDemo;

	private static final Logger logger = LoggerFactory.getLogger(ArtifactMessageHandler.class);

	public ArtifactMessageHandler(SelfDescriptionService selfDescriptionService, ThreadService threadService,
			@Value("${application.dataLakeDirectory}") Path dataLakeDirectory,
			@Value("${application.contract.negotiation.demo}") Boolean contractNegotiationDemo,
			@Value("#{new Boolean('${application.encodePayload:false}')}") Boolean encodePayload) {
		this.selfDescriptionService = selfDescriptionService;
		this.threadService = threadService;
		this.dataLakeDirectory = dataLakeDirectory;
		this.contractNegotiationDemo = contractNegotiationDemo;
		this.encodePayload = encodePayload;
	}

	@Override
	public Map<String, Object> handleMessage(Message message, Object payload) {
		logger.info("Handling header through ArtifactMessageHandler");

		ArtifactRequestMessage arm = (ArtifactRequestMessage) message;
		Message artifactResponseMessage = null;
		Map<String, Object> response = new HashMap<>();

		if (arm.getRequestedArtifact() != null) {

			logger.debug("Handling message with requestedElement:" + arm.getRequestedArtifact());
			if (Boolean.TRUE.equals(((Boolean) threadService.getThreadLocalValue("wss")))) {
				logger.debug("Handling message with requestedElement:" + arm.getRequestedArtifact() + " in WSS flow");
				payload = handleWssFlow(message);
			} else {
				logger.debug("Handling message with requestedElement:" + arm.getRequestedArtifact() + " in REST flow");
				payload = handleRestFlow(message);
			}
		} else {
			logger.error("Artifact requestedElement not provided", message);

			throw new BadParametersException("Artifact requestedElement not provided", message);
		}

		artifactResponseMessage = createArtifactResponseMessage(arm);
		response.put(DataAppMessageHandler.HEADER, artifactResponseMessage);
		response.put(DataAppMessageHandler.PAYLOAD, payload);

		return response;
	}

	private String handleWssFlow(Message message) {
		String reqArtifact = ((ArtifactRequestMessage) message).getRequestedArtifact().getPath();
		String requestedArtifact = reqArtifact.substring(reqArtifact.lastIndexOf('/') + 1);

		if (contractNegotiationDemo) {
			logger.info("WSS Demo, reading directly from data lake");
			return readFile(requestedArtifact, message);
		} else {
			if (selfDescriptionService.artifactRequestedElementExist((ArtifactRequestMessage) message,
					selfDescriptionService.getSelfDescription(message))) {
				return readFile(requestedArtifact, message);
			} else {
				logger.error("Artifact requestedElement not exist in self description", message);

				throw new NotFoundException("Artifact requestedElement not found in self description", message);
			}
		}
	}

	private String readFile(String requestedArtifact, Message message) {
		logger.info("Reading file {} from datalake", requestedArtifact);
		byte[] fileContent;
		try {
			fileContent = Files.readAllBytes(dataLakeDirectory.resolve(requestedArtifact));
		} catch (IOException e) {
			logger.error("Could't read the file {} from datalake", requestedArtifact, message);

			throw new NotFoundException("Could't read the file from datalake", message);

		}
		String base64EncodedFile = Base64.getEncoder().encodeToString(fileContent);
		logger.info("File read from disk.");
		return base64EncodedFile;
	}

	private String handleRestFlow(Message message) {
		String payload = null;
		// Check if requested artifact exist in self description
		if (contractNegotiationDemo || selfDescriptionService.artifactRequestedElementExist((ArtifactRequestMessage) message,
				selfDescriptionService.getSelfDescription(message))) {
			if (isBigPayload(((ArtifactRequestMessage) message).getRequestedArtifact().toString())) {
				payload = encodePayload == true ? encodePayload(BigPayload.BIG_PAYLOAD.getBytes())
						: BigPayload.BIG_PAYLOAD;
				return payload;
			} else {
				payload = encodePayload == true ? encodePayload(createResponsePayload().getBytes())
						: createResponsePayload();
				return payload;
			}
		} else {
			logger.error("Artifact requestedElement not exist in self description", message);

			throw new NotFoundException("Artifact requestedElement not found in self description", message);
		}
	}

	private boolean isBigPayload(String path) {
		String isBig = path.substring(path.lastIndexOf('/'));
		if (isBig.equals("/big")) {
			return true;
		}

		return false;
	}

	private String encodePayload(byte[] payload) {
		logger.info("Encoding payload");

		return Base64.getEncoder().encodeToString(payload);
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

	private Message createArtifactResponseMessage(ArtifactRequestMessage header) {
		// Need to set transferCotract from original message, it will be used in policy
		// enforcement
		return new ArtifactResponseMessageBuilder()._issuerConnector_(whoIAmEngRDProvider())._issued_(DateUtil.normalizedDateTime())
				._modelVersion_(UtilMessageService.MODEL_VERSION)._transferContract_(header.getTransferContract())
				._senderAgent_(whoIAmEngRDProvider())
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._securityToken_(UtilMessageService.getDynamicAttributeToken()).build();
	}
}

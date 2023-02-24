package it.eng.idsa.dataapp.handler;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.util.BigPayload;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

@Component
public class ArtifactMessageHandler extends DataAppMessageHandler {

	@Value("#{new Boolean('${application.encodePayload:false}')}")
	private Boolean encodePayload;
	
	@Value("${application.dataLakeDirectory}")
	private Path dataLakeDirectory;

	private static final Logger logger = LoggerFactory.getLogger(ArtifactMessageHandler.class);

	@Override
	public Map<String, Object> handleMessage(Message artifactRequest, Object payload) {
		logger.info("Handling header through ArtifactMessageHandler");

		ArtifactRequestMessage arm = (ArtifactRequestMessage) artifactRequest;
		Message artifactResponseMessage = null;
		Map<String, Object> response = new HashMap<>();

		if (arm.getRequestedArtifact() != null) {
			logger.debug("Handling message with requestedElement:" + arm.getRequestedArtifact());

			if (isBigPayload(arm.getRequestedArtifact().toString())) {
				payload = encodePayload == true ? encodePayload(BigPayload.BIG_PAYLOAD.getBytes())
						: BigPayload.BIG_PAYLOAD;
			} else {
				payload = encodePayload == true ? encodePayload(createResponsePayload(null).getBytes())
						: createResponsePayload(arm.getRequestedArtifact());
			}
		} else {
			logger.debug("Handling message without requestedElement");

			payload = encodePayload == true ? encodePayload(createResponsePayload(null).getBytes())
					: createResponsePayload(null);
		}

		artifactResponseMessage = createArtifactResponseMessage(arm);
		response.put(DataAppMessageHandler.HEADER, artifactResponseMessage);
		response.put(DataAppMessageHandler.PAYLOAD, payload);

		return response;
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

	private String createResponsePayload(URI uri) {
//		// Put check sum in the payload
//		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		Date date = new Date();
//		String formattedDate = dateFormat.format(date);
//
//		Map<String, String> jsonObject = new HashMap<>();
//		jsonObject.put("firstName", "John");
//		jsonObject.put("lastName", "Doe");
//		jsonObject.put("dateOfBirth", formattedDate);
//		jsonObject.put("address", "591  Franklin Street, Pennsylvania");
//		jsonObject.put("checksum", "ABC123 " + formattedDate);
//		Gson gson = new GsonBuilder().create();
//
//		return gson.toJson(jsonObject);
		
		try {
			return readFile(uri.getRawPath().substring(uri.getRawPath().lastIndexOf('/') + 1));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public Message createArtifactResponseMessage(ArtifactRequestMessage header) {
		// Need to set transferCotract from original message, it will be used in policy
		// enforcement
		return new ArtifactResponseMessageBuilder()._issuerConnector_(whoIAmEngRDProvider())._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)._transferContract_(header.getTransferContract())
				._senderAgent_(whoIAmEngRDProvider())
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._securityToken_(UtilMessageService.getDynamicAttributeToken()).build();
	}
	
	private String readFile(String requestedArtifact) throws IOException {
		logger.info("Reading file {} from datalake", requestedArtifact);
		byte[] fileContent = Files.readAllBytes(dataLakeDirectory.resolve(requestedArtifact));
		String base64EncodedFile = Base64.getEncoder().encodeToString(fileContent);
		logger.info("File read from disk.");
		return base64EncodedFile;
	}
}

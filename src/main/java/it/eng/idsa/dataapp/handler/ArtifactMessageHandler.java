package it.eng.idsa.dataapp.handler;

import static de.fraunhofer.iais.eis.util.Util.asList;

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
import it.eng.idsa.dataapp.util.BigPayload;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

@Component
public class ArtifactMessageHandler extends DataAppMessageHandler {

	@Value("#{new Boolean('${application.encodePayload:false}')}")
	private Boolean encodePayload;

	private static final Logger logger = LoggerFactory.getLogger(ArtifactMessageHandler.class);

	@Override
	public Map<String, Object> handleMessage(Message artifactRequest, Object payload) {
		logger.info("Handling header through ArtifactMessageHandler");

		ArtifactRequestMessage arm = (ArtifactRequestMessage) artifactRequest;
		Message artifactResponseMessage = null;
		Map<String, Object> response = new HashMap<>();

		if (arm.getRequestedArtifact() != null) {
			if (isBigPayload(arm.getRequestedArtifact().toString())) {
				payload = encodePayload == true ? encodePayload(BigPayload.BIG_PAYLOAD.getBytes())
						: BigPayload.BIG_PAYLOAD;
			} else {
				payload = encodePayload == true ? encodePayload(createResponsePayload().getBytes())
						: createResponsePayload();
			}
		} else {
			payload = encodePayload == true ? encodePayload(createResponsePayload().getBytes())
					: createResponsePayload();
		}

		if (payload != null) {
			artifactResponseMessage = createArtifactResponseMessage(arm);
			response.put("header", artifactResponseMessage);
			response.put("payload", payload);
		}

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
}

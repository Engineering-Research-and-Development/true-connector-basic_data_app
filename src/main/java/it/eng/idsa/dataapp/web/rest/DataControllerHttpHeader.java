package it.eng.idsa.dataapp.web.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.DescriptionResponseMessage;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.dataapp.util.MessageUtil;

@Controller
@ConditionalOnProperty(name = "application.dataapp.http.config", havingValue = "http-header")
public class DataControllerHttpHeader {

	private static final Logger logger = LoggerFactory.getLogger(DataControllerHttpHeader.class);
	
	private MessageUtil messageUtil;
	
	public DataControllerHttpHeader(MessageUtil messageUtil) {
		this.messageUtil = messageUtil;
	}

	@PostMapping(value = "/data")
	public ResponseEntity<?> routerHttpHeader(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody(required = false) String payload) {

		logger.info("Http Header request");
		logger.info("headers=" + httpHeaders);
		if (payload != null) {
			logger.info("payload lenght = " + payload.length());
		} else {
			logger.info("Payload is empty");
		}

		HttpHeaders responseHeaders = createResponseMessageHeaders(httpHeaders);
		String responsePayload = null;
		
		if (!("ids:RejectionMessage".equals(responseHeaders.get("IDS-Messagetype").get(0)))) {
			responsePayload = messageUtil.createResponsePayload(httpHeaders);
		} else {
			responsePayload = "Rejected message";
		}
		
		if (responsePayload != null && responsePayload.contains("IDS-RejectionReason")) {
			httpHeaders.put("IDS-Messagetype", Util.asList("ids:RejectionMessage"));
			httpHeaders.put("IDS-RejectionReason", Util.asList(responsePayload.substring(responsePayload.indexOf(":")+1)));
			responseHeaders = createResponseMessageHeaders(httpHeaders);
			responsePayload = "Rejected message";
		}

		return ResponseEntity.ok().header("foo", "bar")
				.headers(responseHeaders)
				.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
				.body(responsePayload);
	}

	private HttpHeaders createResponseMessageHeaders(HttpHeaders httpHeaders) {
		String requestMessageType = httpHeaders.getFirst("IDS-Messagetype");
		HttpHeaders headers = new HttpHeaders();

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date date = new Date();
		String formattedDate = dateFormat.format(date);

		String responseMessageType = null;
		String rejectionReason = null;

		switch (requestMessageType) {
		case "ids:ContractRequestMessage":
			responseMessageType = ContractAgreementMessage.class.getSimpleName();
			break;

		case "ids:ArtifactRequestMessage":
			if (httpHeaders.containsKey("IDS-TransferContract")
					&& !(MultiPartMessageService.DEFAULT_CONTRACT_AGREEMENT
							.equals(httpHeaders.get("IDS-TransferContract").get(0)))
					&& MultiPartMessageService.DEFAULT_TARGET_ARTIFACT
					.equals(httpHeaders.get("IDS-RequestedArtifact").get(0))) {
				responseMessageType = RejectionMessage.class.getSimpleName();
				rejectionReason = "https://w3id.org/idsa/code/NOT_AUTHORIZED";
			} else {
				responseMessageType = ArtifactResponseMessage.class.getSimpleName();
			}
			break;
			
		case "ids:DescriptionRequestMessage":
			responseMessageType = DescriptionResponseMessage.class.getSimpleName();
			break;
			
		case "ids:RejectionMessage":
			responseMessageType = requestMessageType.substring(4);
			rejectionReason = httpHeaders.get("IDS-RejectionReason").get(0);
			break;

		default:
			break;
		}
		
		if (responseMessageType != null) {
			headers.add("IDS-Messagetype", "ids:" + responseMessageType);
		}
		headers.add("IDS-Issued", formattedDate);
		headers.add("IDS-IssuerConnector", "http://w3id.org/engrd/connector");
		headers.add("IDS-CorrelationMessage", "https://w3id.org/idsa/autogen/"+ responseMessageType +"/"+ UUID.randomUUID().toString());
		headers.add("IDS-ModelVersion", "4.0.0");
		headers.add("IDS-Id", "https://w3id.org/idsa/autogen/"+ responseMessageType +"/"+ UUID.randomUUID().toString());
		if (rejectionReason != null) {
			headers.add("IDS-RejectionReason", rejectionReason);
		}

		return headers;
	}

}

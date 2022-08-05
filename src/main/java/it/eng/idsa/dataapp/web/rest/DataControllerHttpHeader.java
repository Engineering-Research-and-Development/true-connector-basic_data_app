package it.eng.idsa.dataapp.web.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessage;
import de.fraunhofer.iais.eis.TokenFormat;
import it.eng.idsa.dataapp.util.MessageUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

@Controller
@ConditionalOnProperty(name = "application.dataapp.http.config", havingValue = "http-header")
public class DataControllerHttpHeader {

	private static final Logger logger = LoggerFactory.getLogger(DataControllerHttpHeader.class);
	
	private MessageUtil messageUtil;
	private String issueConnector;
	public DataControllerHttpHeader(MessageUtil messageUtil,
			@Value("${application.ecc.issuer.connector}") String issuerConnector) {
		super();
		this.messageUtil = messageUtil;
		this.issueConnector = issuerConnector;
	}

	@PostMapping(value = "/data")
	public ResponseEntity<?> routerHttpHeader(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody(required = false) String payload) {

		logger.info("Http Header request");
		logger.info("headers=" + httpHeaders);
		if (payload != null) {
			logger.info("payload lenght = " + payload.length());
			logger.debug("payload=" + payload);
		} else {
			logger.info("Payload is empty");
		}

		HttpHeaders responseHeaders = createResponseMessageHeaders(httpHeaders, null);
		String responsePayload = null;
		
		if (!("ids:RejectionMessage".equals(responseHeaders.get("IDS-Messagetype").get(0)))) {
			responsePayload = messageUtil.createResponsePayload(httpHeaders, payload);
		}
		if(responsePayload == null && 
				"ids:ContractRequestMessage".equals(responseHeaders.get("IDS-Messagetype").get(0))) {
			logger.info("Creating rejection message since contract agreement was not found");
			responseHeaders = createResponseMessageHeaders(httpHeaders, "NOT-FOUND");
		}	
		
		if (responsePayload != null && responsePayload.contains("IDS-RejectionReason")) {
			responseHeaders = createResponseMessageHeaders(httpHeaders, responsePayload.substring(responsePayload.indexOf(":")+1));
			responsePayload = null;
		}
		
		
		ResponseEntity<?> response = ResponseEntity.noContent()
				.headers(responseHeaders)
				.build();
		
		MediaType payloadContentType = MediaType.TEXT_PLAIN;
		
		if(responsePayload != null && responsePayload.contains("John")) {
			payloadContentType = MediaType.APPLICATION_JSON;
		}
		
		// returns John Doe if everything is OK
		if (!("ids:RejectionMessage".equals(responseHeaders.get("IDS-Messagetype").get(0)))) {
			response = ResponseEntity.ok()
					.headers(responseHeaders)
					.contentType(payloadContentType)
					.body(responsePayload);
		}
		
		return response;
	}

	private HttpHeaders createResponseMessageHeaders(HttpHeaders httpHeaders, String rejectionReason) {
		String requestMessageType = httpHeaders.getFirst("IDS-Messagetype");
		HttpHeaders headers = new HttpHeaders();

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date date = new Date();
		String formattedDate = dateFormat.format(date);

		String responseMessageType = null;

		if (StringUtils.isNotEmpty(rejectionReason)) {
			requestMessageType = "ids:RejectionMessage";
		}
		
		switch (requestMessageType) {
		case "ids:ContractRequestMessage":
			responseMessageType = ContractAgreementMessage.class.getSimpleName();
			break;

		case "ids:ArtifactRequestMessage":
			responseMessageType = ArtifactResponseMessage.class.getSimpleName();
			headers.add("IDS-TransferContract", httpHeaders.getFirst("IDS-TransferContract"));
			break;
			
		case "ids:DescriptionRequestMessage":
			responseMessageType = DescriptionResponseMessage.class.getSimpleName();
			break;
			
		case "ids:ContractAgreementMessage":
			responseMessageType = MessageProcessedNotificationMessage.class.getSimpleName();
			break;
			
		case "ids:RejectionMessage":
			responseMessageType = requestMessageType.substring(4);
			break;

		default:
			break;
		}
		
		if (StringUtils.isNotEmpty(responseMessageType)) {
			headers.add("IDS-Messagetype", "ids:" + responseMessageType);
		}
		headers.add("IDS-Issued", formattedDate);
		headers.add("IDS-IssuerConnector", issueConnector);
		headers.add("IDS-CorrelationMessage", httpHeaders.getFirst("IDS-Id"));
		headers.add("IDS-ModelVersion", UtilMessageService.MODEL_VERSION);
		headers.add("IDS-Id", "https://w3id.org/idsa/autogen/"+ responseMessageType +"/"+ UUID.randomUUID().toString());
		headers.add("IDS-SenderAgent", "https://sender.agent.com");
		
		headers.add("IDS-SecurityToken-Type", "ids:DynamicAttributeToken");
		headers.add("IDS-SecurityToken-Id", "https://w3id.org/idsa/autogen/" + UUID.randomUUID());
		headers.add("IDS-SecurityToken-TokenFormat", TokenFormat.JWT.getId().toString());
		headers.add("IDS-SecurityToken-TokenValue", UtilMessageService.TOKEN_VALUE);
		if (StringUtils.isNotEmpty(rejectionReason)) {
			headers.add("IDS-RejectionReason", rejectionReason);
		}
		
		headers.add("foo", "bar");

		return headers;
	}

}

package it.eng.idsa.dataapp.web.rest;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.RejectionMessage;
import it.eng.idsa.dataapp.util.MessageUtil;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.UtilMessageService;

@Controller
@ConditionalOnProperty(name = "application.dataapp.http.config", havingValue = "mixed")
public class DataControllerBodyBinary {

	private static final Logger logger = LoggerFactory.getLogger(DataControllerBodyBinary.class);
	
	private MessageUtil messageUtil;
	
	public DataControllerBodyBinary(MessageUtil messageUtil) {
		this.messageUtil = messageUtil;
	}
	
	@PostMapping(value = "/data")
	public ResponseEntity<?> routerBinary(@RequestHeader HttpHeaders httpHeaders,
			@RequestPart(value = "header") String headerMessage,
			@RequestHeader(value = "Response-Type", required = false) String responseType,
			@RequestPart(value = "payload", required = false) String payload) throws JsonProcessingException {

		logger.info("Multipart/mixed request");

		// Convert de.fraunhofer.iais.eis.Message to the String
//		String headerSerialized = new Serializer().serializePlainJson(headerMessage);
		logger.info("header=" + headerMessage);
		logger.info("headers=" + httpHeaders);
		if (payload != null) {
			logger.info("payload length = " + payload.length());
		} else {
			logger.info("Payload is empty");
		}
		
		Message message = MultipartMessageProcessor.getMessage(headerMessage);

		Message headerResponse = messageUtil.getResponseHeader(message);
		String responsePayload = null;
		
		if (!(headerResponse instanceof RejectionMessage)) {
			responsePayload = messageUtil.createResponsePayload(message, payload);
		} 
		if(responsePayload == null && message instanceof ContractRequestMessage) {
			logger.info("Creating rejection message since contract agreement was not found");
			headerResponse = UtilMessageService.getRejectionMessage(RejectionReason.NOT_FOUND);
		}	
		
		if (responsePayload != null && responsePayload.contains("ids:rejectionReason")) {
			headerResponse = MultipartMessageProcessor.getMessage(responsePayload);
			responsePayload = null;
		}
		MultipartMessage responseMessage = new MultipartMessageBuilder()
				.withHeaderContent(headerResponse)
				.withPayloadContent(responsePayload)
				.build();
		String responseMessageString = MultipartMessageProcessor.multipartMessagetoString(responseMessage, false, Boolean.TRUE);
		
		Optional<String> boundary = MultipartMessageProcessor.getMessageBoundaryFromMessage(responseMessageString);
		String contentType = "multipart/mixed; boundary=" + boundary.orElse("---aaa") + ";charset=UTF-8";

		return ResponseEntity.ok()
				.header("foo", "bar")
				.contentType(MediaType.parseMediaType(contentType))
				.body(responseMessageString);
	}
}

package it.eng.idsa.dataapp.web.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
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

import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessage;
import it.eng.idsa.dataapp.util.MessageUtil;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

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
			@RequestPart(value = "payload", required = false) String payload) throws IOException {

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
			headerResponse = messageUtil.createRejectionCommunicationLocalIssues(message);
		}	
		
		if (responsePayload != null && responsePayload.contains("ids:rejectionReason")) {
			headerResponse = MultipartMessageProcessor.getMessage(responsePayload);
			responsePayload = null;
		}

		ContentType payloadContentType = ContentType.TEXT_PLAIN;
		
		if(responsePayload != null && responsePayload.contains("John")) {
			payloadContentType = ContentType.APPLICATION_JSON;
		}
		
		HttpEntity resultEntity = messageUtil.createMultipartMessageForm(
				MultipartMessageProcessor.serializeToJsonLD(headerResponse),
				responsePayload,
				payloadContentType);
		String contentType = resultEntity.getContentType().getValue();
		contentType = contentType.replace("multipart/form-data", "multipart/mixed");
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        resultEntity.writeTo(outStream);
        outStream.flush();
        
		return ResponseEntity.ok()
				.header("foo", "bar")
				.contentType(MediaType.parseMediaType(contentType))
				.body(outStream.toByteArray());
	}
}

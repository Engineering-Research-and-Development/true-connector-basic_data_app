package it.eng.idsa.dataapp.web.rest;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.eng.idsa.dataapp.util.MessageUtil;

@RestController
@ConditionalOnProperty(name = "application.dataapp.http.config", havingValue = "form")
public class DataControllerBodyForm {
	private static final Logger logger = LoggerFactory.getLogger(DataControllerBodyForm.class);

	private MessageUtil messageUtil;
	
	public DataControllerBodyForm(MessageUtil messageUtil) {
		this.messageUtil= messageUtil;
    }

	@PostMapping(value = "/data")
	public ResponseEntity<?> routerForm(@RequestHeader HttpHeaders httpHeaders,
			@RequestParam(value = "header") String header,
			@RequestHeader(value = "Response-Type", required = false) String responseType,
			@RequestParam(value = "payload", required = false) String payload)
			throws UnsupportedOperationException, IOException {

		logger.info("Multipart/form request");

		// Received "header" and "payload"
		logger.info("header" + header);
		logger.info("headers=" + httpHeaders);
		if (payload != null) {
			logger.info("payload lenght = " + payload.length());
		} else {
			logger.info("Payload is empty");
		}
		
		String headerResponse = messageUtil.getResponseHeader(header);
		String responsePayload = null;
		if (!headerResponse.contains("ids:rejectionReason")) {
			responsePayload = messageUtil.createResponsePayload(header);
		}
		
		if (responsePayload != null && responsePayload.contains("ids:rejectionReason")) {
			headerResponse = responsePayload;
			responsePayload = null;
		}

		// prepare body response - multipart message.
		HttpEntity resultEntity = messageUtil.createMultipartMessageForm(
				headerResponse,
				responsePayload,
				null,
				ContentType.APPLICATION_JSON);

		return ResponseEntity.ok()
				.header("foo", "bar")
				.contentType(MediaType.parseMediaType(resultEntity.getContentType().getValue()))
				.body(resultEntity.getContent().readAllBytes());
	}
}

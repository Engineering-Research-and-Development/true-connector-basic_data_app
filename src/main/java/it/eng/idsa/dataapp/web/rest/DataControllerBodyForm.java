package it.eng.idsa.dataapp.web.rest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

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
import org.springframework.web.multipart.MultipartFile;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessage;
import it.eng.idsa.dataapp.util.MessageUtil;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@RestController
@ConditionalOnProperty(name = "application.dataapp.http.config", havingValue = "form")
public class DataControllerBodyForm {
	private static final Logger logger = LoggerFactory.getLogger(DataControllerBodyForm.class);

	private MessageUtil messageUtil;
	
	public DataControllerBodyForm(MessageUtil messageUtil) {
		this.messageUtil= messageUtil;
    }

	
	/**
	 * 
	 * @param httpHeaders
	 * @param header
	 * @param responseType
	 * @param payload Could be MultipartFile or plain String
	 * @return
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	@PostMapping(value = "/data")
	public ResponseEntity<?> routerForm(@RequestHeader HttpHeaders httpHeaders,
			@RequestParam(value = "header") String header,
			@RequestHeader(value = "Response-Type", required = false) String responseType,
			@RequestParam(value = "payload", required = false) Object payload)
			throws UnsupportedOperationException, IOException {

		logger.info("Multipart/form request");

		// Received "header" and "payload"
		logger.info("header" + header);
		logger.info("headers=" + httpHeaders);
		if (payload != null) {
			logger.info("payload lenght = " + payload.toString().length());
		} else {
			logger.info("Payload is empty");
		}
		
		
		if (payload instanceof MultipartFile) {
			MultipartFile file = (MultipartFile) payload;
			try (FileOutputStream fos = new FileOutputStream(file.getOriginalFilename())) {
				byte[] decoder = Base64.getDecoder().decode(file.getBytes());
				fos.write(decoder);
			}
		}
		
		Message message = MultipartMessageProcessor.getMessage(header);

		Message headerResponse = messageUtil.getResponseHeader(message);
		String responsePayload = null;
		if (!(headerResponse instanceof RejectionMessage)) {
			responsePayload = messageUtil.createResponsePayload(message);
		}
		
		if (responsePayload != null && responsePayload.contains("ids:rejectionReason")) {
			headerResponse = MultipartMessageProcessor.getMessage(responsePayload);
			responsePayload = null;
		}

		// prepare body response - multipart message.
		HttpEntity resultEntity = messageUtil.createMultipartMessageForm(
				MultipartMessageProcessor.serializeToJsonLD(headerResponse),
				responsePayload,
				null,
				ContentType.APPLICATION_JSON);

		return ResponseEntity.ok()
				.header("foo", "bar")
				.contentType(MediaType.parseMediaType(resultEntity.getContentType().getValue()))
				.body(resultEntity.getContent().readAllBytes());
	}
}

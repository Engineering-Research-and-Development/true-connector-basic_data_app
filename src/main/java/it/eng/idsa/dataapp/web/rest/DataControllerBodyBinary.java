package it.eng.idsa.dataapp.web.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

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

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.handler.DataAppMessageHandler;
import it.eng.idsa.dataapp.handler.MessageHandlerFactory;
import it.eng.idsa.dataapp.util.MessageUtil;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Controller
@ConditionalOnProperty(name = "application.dataapp.http.config", havingValue = "mixed")
public class DataControllerBodyBinary {

	private static final Logger logger = LoggerFactory.getLogger(DataControllerBodyBinary.class);

	private MessageUtil messageUtil;
	private MessageHandlerFactory factory;

	public DataControllerBodyBinary(MessageUtil messageUtil, MessageHandlerFactory factory) {
		this.messageUtil = messageUtil;
		this.factory = factory;
	}

	@PostMapping(value = "/data")
	public ResponseEntity<?> routerBinary(@RequestHeader HttpHeaders httpHeaders,
			@RequestPart(value = "header") String headerMessage,
			@RequestHeader(value = "Response-Type", required = false) String responseType,
			@RequestPart(value = "payload", required = false) String payload) throws IOException {

		logger.info("Multipart/mixed request");

		Message message = MultipartMessageProcessor.getMessage(headerMessage);

		// Create handler based on type of message and get map with header and payload
		DataAppMessageHandler handler = factory.createMessageHandler(message.getClass());
		Map<String, Object> responseMap = handler.handleMessage(message, payload);
		Object responseHeader = responseMap.get(DataAppMessageHandler.HEADER);
		Object responsePayload = responseMap.get(DataAppMessageHandler.PAYLOAD);
		ContentType payloadContentType = ContentType.TEXT_PLAIN;

		if (responsePayload != null && messageUtil.isValidJSON(responsePayload.toString())) {
			payloadContentType = ContentType.APPLICATION_JSON;
		}

		HttpEntity resultEntity = messageUtil.createMultipartMessageForm(
				MultipartMessageProcessor.serializeToJsonLD(responseHeader),
				responsePayload != null ? responsePayload.toString() : null, payloadContentType);

		String contentType = resultEntity.getContentType().getValue();
		contentType = contentType.replace("multipart/form-data", "multipart/mixed");

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		resultEntity.writeTo(outStream);
		outStream.flush();

		return ResponseEntity.ok().header("foo", "bar").contentType(MediaType.parseMediaType(contentType))
				.body(outStream.toByteArray());
	}
}

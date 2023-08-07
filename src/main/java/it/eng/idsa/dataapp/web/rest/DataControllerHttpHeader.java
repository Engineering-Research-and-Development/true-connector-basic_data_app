package it.eng.idsa.dataapp.web.rest;

import java.util.Map;

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

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.handler.DataAppMessageHandler;
import it.eng.idsa.dataapp.handler.MessageHandlerFactory;
import it.eng.idsa.dataapp.util.HttpHeadersUtil;
import it.eng.idsa.dataapp.util.MessageUtil;

@Controller
@ConditionalOnProperty(name = "application.dataapp.http.config", havingValue = "http-header")
public class DataControllerHttpHeader {

	private static final Logger logger = LoggerFactory.getLogger(DataControllerHttpHeader.class);

	private MessageUtil messageUtil;
	private MessageHandlerFactory factory;

	public DataControllerHttpHeader(MessageUtil messageUtil, MessageHandlerFactory factory) {
		super();
		this.messageUtil = messageUtil;
		this.factory = factory;
	}

	@PostMapping(value = "/data")
	public ResponseEntity<?> routerHttpHeader(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody(required = false) String payload) throws ClassNotFoundException {

		logger.info("Http Header request");

		HttpHeaders responseHeaders = new HttpHeaders();

		Message message = HttpHeadersUtil.httpHeadersToMessage(httpHeaders);
		// Create handler based on type of message and get map with header and payload
		DataAppMessageHandler handler = factory.createMessageHandler(message.getClass());
		Map<String, Object> responseMap = handler.handleMessage(message, payload);
		Object responseHeader = responseMap.get(DataAppMessageHandler.HEADER);
		Object responsePayload = responseMap.get(DataAppMessageHandler.PAYLOAD);
		if (responseHeader != null) {
			responseHeaders = HttpHeadersUtil.messageToHttpHeaders((Message) responseHeader);
		}

		ResponseEntity<?> response = ResponseEntity.noContent().headers(responseHeaders).build();

		MediaType payloadContentType = MediaType.TEXT_PLAIN;

		if (responsePayload != null && messageUtil.isValidJSON(responsePayload.toString())) {
			payloadContentType = MediaType.APPLICATION_JSON;
		}

		response = ResponseEntity.ok().headers(responseHeaders).contentType(payloadContentType).body(responsePayload);

		return response;
	}
}

package it.eng.idsa.dataapp.web.rest;

import java.util.Enumeration;
import java.util.HashMap;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.context.request.NativeWebRequest;

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
	private HttpHeadersUtil httpHeadersUtil;

	public DataControllerHttpHeader(MessageUtil messageUtil, MessageHandlerFactory factory,
			HttpHeadersUtil httpHeadersUtil) {
		super();
		this.messageUtil = messageUtil;
		this.factory = factory;
		this.httpHeadersUtil = httpHeadersUtil;
	}

	@PostMapping(value = "/data")
	public ResponseEntity<?> routerHttpHeader(@RequestHeader HttpHeaders httpHeaders, HttpServletRequest request,
			NativeWebRequest webRequest, @RequestBody(required = false) String payload) {

		HttpServletRequest requestNative = webRequest.getNativeRequest(HttpServletRequest.class);
		Enumeration<String> headerNamesNative = request.getHeaderNames();
		Enumeration<String> headerNames = request.getHeaderNames();

		logger.info("Http Header request");
		logger.info("headers=" + httpHeaders);
		if (payload != null) {
			logger.info("payload lenght = " + payload.length());
			logger.debug("payload=" + payload);
		} else {
			logger.info("Payload is empty");
		}

		HttpHeaders responseHeaders = new HttpHeaders();

		Map<String, Object> headersMap = httpHeadersUtil.httpHeadersToMap(httpHeaders);

		Message message = httpHeadersUtil.headersToMessage(headersMap);

		// Create handler based on type of message and get map with header and payload
		DataAppMessageHandler handler = factory.createMessageHandler(message.getClass());
		Map<String, Object> responseMap = handler.handleMessage(message, payload);
		Map<String, Object> responseHeaderMap = new HashMap<>();
		if (responseMap.get("header") != null) {
			responseHeaderMap = httpHeadersUtil.messageToHeaders((Message) responseMap.get("header"));
			responseHeaders = httpHeadersUtil.createResponseMessageHeaders(responseHeaderMap);
		}

		ResponseEntity<?> response = ResponseEntity.noContent().headers(responseHeaders).build();

		MediaType payloadContentType = MediaType.TEXT_PLAIN;

		if (responseMap.get("payload") != null && messageUtil.isValidJSON(responseMap.get("payload").toString())) {
			payloadContentType = MediaType.APPLICATION_JSON;
		}

		response = ResponseEntity.ok().headers(responseHeaders).contentType(payloadContentType)
				.body(responseMap.get("payload"));

		return response;
	}
}

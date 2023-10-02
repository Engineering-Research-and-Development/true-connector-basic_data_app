package it.eng.idsa.dataapp.web.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import de.fraunhofer.iais.eis.DescriptionResponseMessage;
import de.fraunhofer.iais.eis.DescriptionResponseMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.handler.DataAppMessageHandler;
import it.eng.idsa.dataapp.handler.DescriptionRequestMessageHandler;
import it.eng.idsa.dataapp.handler.MessageHandlerFactory;
import it.eng.idsa.dataapp.util.HttpHeadersUtil;
import it.eng.idsa.dataapp.util.MessageUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

class DataControllerHttpHeaderTest {

	@InjectMocks
	private DataControllerHttpHeader controller;
	@Mock
	private MessageUtil messageUtil;
	@Mock
	private MessageHandlerFactory factory;
	@Mock
	private DescriptionRequestMessageHandler handler;
	@Mock
	private Map<String, Object> responseMap;
	private Message message;
	private HttpHeaders httpHeaders;

	@BeforeEach
	public void setUp() throws ClassNotFoundException {
		MockitoAnnotations.openMocks(this);
		message = UtilMessageService.getDescriptionRequestMessage(null);
		httpHeaders = HttpHeadersUtil.messageToHttpHeaders(message);
		when(handler.handleMessage(any(), any())).thenReturn(responseMap);
		when(responseMap.get(DataAppMessageHandler.HEADER)).thenReturn(getDescriptionResponseMessage());
		when(responseMap.get(DataAppMessageHandler.PAYLOAD)).thenReturn("payload");
		when(factory.createMessageHandler(any())).thenReturn(handler);
		when(messageUtil.isValidJSON(responseMap.get(DataAppMessageHandler.PAYLOAD).toString())).thenReturn(true);
	}

	@Test
	public void routerBinaryWithNonNullPayloadTest() throws Exception {

		String payload = "payload";

		ResponseEntity<?> response = controller.routerHttpHeader(httpHeaders, payload);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
	}

	@Test
	public void routerBinaryWithNullPayloadTest() throws Exception {

		String payload = null;

		ResponseEntity<?> response = controller.routerHttpHeader(httpHeaders, payload);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
	}

	@Test
	public void routerBinaryWithHeaderPayloadNullTest() throws Exception {

		String payload = null;

		when(responseMap.get(DataAppMessageHandler.HEADER)).thenReturn(null);
		when(responseMap.get(DataAppMessageHandler.PAYLOAD)).thenReturn(null);

		ResponseEntity<?> response = controller.routerHttpHeader(httpHeaders, payload);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
	}

	private DescriptionResponseMessage getDescriptionResponseMessage() {
		return new DescriptionResponseMessageBuilder()._issued_(UtilMessageService.ISSUED)
				._issuerConnector_(UtilMessageService.ISSUER_CONNECTOR)._modelVersion_(UtilMessageService.MODEL_VERSION)
				._correlationMessage_(UtilMessageService.CORRELATION_MESSAGE)
				._senderAgent_(UtilMessageService.SENDER_AGENT)
				._securityToken_(UtilMessageService.getDynamicAttributeToken()).build();
	}
}

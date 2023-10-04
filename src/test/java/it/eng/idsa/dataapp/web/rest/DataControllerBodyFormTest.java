package it.eng.idsa.dataapp.web.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.handler.DataAppMessageHandler;
import it.eng.idsa.dataapp.handler.DescriptionRequestMessageHandler;
import it.eng.idsa.dataapp.handler.MessageHandlerFactory;
import it.eng.idsa.dataapp.util.MessageUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

class DataControllerBodyFormTest {

	@InjectMocks
	private DataControllerBodyForm controller;
	@Mock
	private MessageUtil messageUtil;
	@Mock
	private MessageHandlerFactory factory;
	@Mock
	private HttpEntity entity;
	@Mock
	private DescriptionRequestMessageHandler handler;
	@Mock
	private HttpHeaders httpHeaders;
	@Mock
	private Header header;
	@Mock
	private Map<String, Object> responseMap;
	private Message message;
	private String headerMessage;
	private String responseType = null;
	private String payload;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		message = UtilMessageService.getDescriptionRequestMessage(null);
		headerMessage = UtilMessageService.getMessageAsString(message);
		payload = "payload";
		when(handler.handleMessage(message, payload)).thenReturn(responseMap);
		when(responseMap.get(DataAppMessageHandler.HEADER)).thenReturn("header");
		when(responseMap.get(DataAppMessageHandler.PAYLOAD)).thenReturn("payload");
		when(messageUtil.isValidJSON(responseMap.get(DataAppMessageHandler.PAYLOAD).toString())).thenReturn(true);
		when(factory.createMessageHandler(any())).thenReturn(handler);
		when(messageUtil.createMultipartMessageForm(any(), any(), any())).thenReturn(entity);
		when(entity.getContentType()).thenReturn(header);
		when(header.getValue()).thenReturn("multipart/form-data");
	}

	@Test
	public void routerBinaryWithNonNullPayloadTest() throws IOException {

		ResponseEntity<?> response = controller.routerForm(httpHeaders, headerMessage, responseType, payload);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.MULTIPART_FORM_DATA, response.getHeaders().getContentType());
	}

	@Test
	public void routerBinaryWithNullPayload() throws Exception {

		payload = null;

		ResponseEntity<?> response = controller.routerForm(httpHeaders, headerMessage, responseType, payload);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.MULTIPART_FORM_DATA, response.getHeaders().getContentType());
	}
}

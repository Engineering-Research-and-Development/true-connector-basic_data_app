package it.eng.idsa.dataapp.web.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.PropertyChangeEvent;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.DescriptionResponseMessage;
import de.fraunhofer.iais.eis.DescriptionResponseMessageBuilder;
import it.eng.idsa.dataapp.handler.DataAppMessageHandler;
import it.eng.idsa.dataapp.handler.DescriptionRequestMessageHandler;
import it.eng.idsa.dataapp.handler.MessageHandlerFactory;
import it.eng.idsa.dataapp.service.ThreadService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.UtilMessageService;

class IncomingDataAppResourceOverWsTest {

	@InjectMocks
	private IncomingDataAppResourceOverWs controller;
	@Mock
	private MessageHandlerFactory factory;
	@Mock
	private DescriptionRequestMessageHandler handler;
	@Mock
	private ThreadService threadService;
	@Mock
	private Map<String, Object> responseMap;
	@Mock
	private PropertyChangeEvent evt;
	private MultipartMessage multipartMessage;
	private String requestMessageMultipart;
	private static final String PAYLOAD = "payload";

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		requestMessageMultipart = createMultipartMessageAsString();
		multipartMessage = createMultipartMessage();
		when(threadService.getThreadLocalValue("wss")).thenReturn(true);
		when(evt.getNewValue()).thenReturn(requestMessageMultipart);
		when(factory.createMessageHandler(any())).thenReturn(handler);
		when(handler.handleMessage(multipartMessage.getHeaderContent(), multipartMessage.getPayloadContent()))
				.thenReturn(responseMap);
		when(responseMap.get(DataAppMessageHandler.PAYLOAD)).thenReturn("payload");
	}

	@Test
	void propertyExchangeTest() {
		when(responseMap.get(DataAppMessageHandler.HEADER)).thenReturn(getDescriptionResponseMessage());
		controller.propertyChange(evt);
		
		verify(threadService).setThreadLocalValue("wss", true);
		verify(handler).handleMessage(any(), any());
	}

	@Test
	void propertyExchangeExceptionTest() {
		when(responseMap.get(DataAppMessageHandler.HEADER)).thenReturn("test");

		Exception exception = assertThrows(Exception.class, () -> {
			controller.propertyChange(evt);
		});
	
		assertNotNull(exception);
	}

	private MultipartMessage createMultipartMessage() {

		return MultipartMessageProcessor.parseMultipartMessage(requestMessageMultipart);
	}

	private String createMultipartMessageAsString() {

		MultipartMessage mm = new MultipartMessageBuilder()
				.withHeaderContent(UtilMessageService.getDescriptionRequestMessage(null)).withPayloadContent(PAYLOAD)
				.build();
		return MultipartMessageProcessor.multipartMessagetoString(mm);
	}

	private DescriptionResponseMessage getDescriptionResponseMessage() {
		return new DescriptionResponseMessageBuilder()._issued_(UtilMessageService.ISSUED)
				._issuerConnector_(UtilMessageService.ISSUER_CONNECTOR)._modelVersion_(UtilMessageService.MODEL_VERSION)
				._correlationMessage_(UtilMessageService.CORRELATION_MESSAGE)
				._senderAgent_(UtilMessageService.SENDER_AGENT)
				._securityToken_(UtilMessageService.getDynamicAttributeToken()).build();
	}
}

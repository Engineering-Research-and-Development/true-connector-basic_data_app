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
import it.eng.idsa.dataapp.util.MessageUtil;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.UtilMessageService;
import it.eng.idsa.streamer.WebSocketServerManager;

class IncomingDataAppResourceOverWsTest {

	@InjectMocks
	private IncomingDataAppResourceOverWs controller;
	@Mock
	private MessageUtil messageUtil;
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
	@Mock
	private WebSocketServerManager webSocketServerManager;
	MultipartMessage multipartMessage;
	String requestMessageMultipart;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		requestMessageMultipart = createReceivedMessage();
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

	private String createReceivedMessage() {

		return "--JF-UsWbL9rqVxjbb20vdhBSlOT3FiZImD8\r\n" + "Content-Disposition: form-data; name=\"header\"\r\n"
				+ "Content-Length: 2506\r\n" + "Content-Type: application/ld+json\r\n" + "\r\n" + "{\r\n"
				+ "  \"@context\" : {\r\n" + "    \"ids\" : \"https://w3id.org/idsa/core/\",\r\n"
				+ "    \"idsc\" : \"https://w3id.org/idsa/code/\"\r\n" + "  },\r\n"
				+ "  \"@type\" : \"ids:DescriptionRequestMessage\",\r\n"
				+ "  \"@id\" : \"https://w3id.org/idsa/autogen/descriptionRequestMessage/f92fa9ca-6519-4d2b-863a-2553a4ff0e86\",\r\n"
				+ "  \"ids:senderAgent\" : {\r\n" + "    \"@id\" : \"http://sender.agent/sender\"\r\n" + "  },\r\n"
				+ "  \"ids:securityToken\" : {\r\n" + "    \"@type\" : \"ids:DynamicAttributeToken\",\r\n"
				+ "    \"@id\" : \"https://w3id.org/idsa/autogen/dynamicAttributeToken/5c446fb7-1630-4006-9b34-dac9aaf2f4ed\",\r\n"
				+ "    \"ids:tokenValue\" : \"1\",\r\n" + "    \"ids:tokenFormat\" : {\r\n"
				+ "      \"@id\" : \"https://w3id.org/idsa/code/JWT\"\r\n" + "    }\r\n" + "  },\r\n"
				+ "  \"ids:issuerConnector\" : {\r\n" + "    \"@id\" : \"http://w3id.org/engrd/connector/\"\r\n"
				+ "  },\r\n" + "  \"ids:modelVersion\" : \"4.1.0\",\r\n" + "  \"ids:issued\" : {\r\n"
				+ "    \"@value\" : \"2023-03-17T11:11:28.044Z\",\r\n"
				+ "    \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + "  },\r\n"
				+ "  \"ids:recipientConnector\" : [ ],\r\n" + "  \"ids:recipientAgent\" : [ ],\r\n"
				+ "  \"ids:requestedElement\" : {\r\n"
				+ "    \"@id\" : \"https://w3id.org/idsa/autogen/textResource/8da1bf26-9e74-46ee-877a-9aaffa19a03d\"\r\n"
				+ "  }\r\n" + "}\r\n" + "--JF-UsWbL9rqVxjbb20vdhBSlOT3FiZImD8--";

	}

	private DescriptionResponseMessage getDescriptionResponseMessage() {
		return new DescriptionResponseMessageBuilder()._issued_(UtilMessageService.ISSUED)
				._issuerConnector_(UtilMessageService.ISSUER_CONNECTOR)._modelVersion_(UtilMessageService.MODEL_VERSION)
				._correlationMessage_(UtilMessageService.CORRELATION_MESSAGE)
				._senderAgent_(UtilMessageService.SENDER_AGENT)
				._securityToken_(UtilMessageService.getDynamicAttributeToken()).build();
	}
}

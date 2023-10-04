package it.eng.idsa.dataapp.handler;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.service.SelfDescriptionService;
import it.eng.idsa.dataapp.web.rest.exceptions.NotFoundException;
import it.eng.idsa.multipart.processor.util.SelfDescriptionUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

class DescriptionRequestMessageHandlerTest {

	@InjectMocks
	private DescriptionRequestMessageHandler descriptionRequestMessageHandler;
	@Mock
	SelfDescriptionService selfDescriptionService;
	private Message message;
	Map<String, Object> responseMap = new HashMap<>();
	private Serializer serializer = new Serializer();
	private Connector baseConnector;
	private String issuerConnector = "http://w3id.org/engrd/connector/";
	private String selfDescriptionAsString;

	@BeforeEach
	public void init() throws IOException, URISyntaxException {

		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(descriptionRequestMessageHandler, "issuerConnector", issuerConnector);
		message = UtilMessageService.getDescriptionRequestMessage(null);
		baseConnector = SelfDescriptionUtil.createDefaultSelfDescription();
		selfDescriptionAsString = serializer.serialize(baseConnector);

	}

	@Test
	void handleMessageTest() throws IOException {

		when(selfDescriptionService.getSelfDescriptionAsString(message)).thenReturn(selfDescriptionAsString);
		when(selfDescriptionService.getSelfDescription(message)).thenReturn(baseConnector);

		responseMap = descriptionRequestMessageHandler.handleMessage(message, "asdsad");

		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}

	@Test
	void handleMessageNotFoundRequestedElementTest() throws URISyntaxException, IOException {

		message = UtilMessageService.getDescriptionRequestMessage(new URI("http://www.google.com"));

		baseConnector = SelfDescriptionUtil.createDefaultSelfDescription();
		when(selfDescriptionService.getSelfDescription(message)).thenReturn(baseConnector);
		when(selfDescriptionService.getRequestedElement((DescriptionRequestMessage) message, baseConnector))
				.thenThrow(new NotFoundException("Requested element not found"));
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			responseMap = descriptionRequestMessageHandler.handleMessage(message, "asdsad");
		});

		assertEquals("Requested element not found", exception.getMessage());
	}
}

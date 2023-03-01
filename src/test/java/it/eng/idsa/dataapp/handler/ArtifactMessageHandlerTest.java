package it.eng.idsa.dataapp.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


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

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.service.SelfDescriptionService;
import it.eng.idsa.dataapp.web.rest.exceptions.BadParametersException;
import it.eng.idsa.dataapp.web.rest.exceptions.NotFoundException;
import it.eng.idsa.multipart.processor.util.SelfDescriptionUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

class ArtifactMessageHandlerTest {

	@InjectMocks
	private ArtifactMessageHandler artifactMessageHandler;
	@Mock
	SelfDescriptionService selfDescriptionService;
	private Message message;
	Map<String, Object> responseMap = new HashMap<>();
	private String issuerConnector = "http://w3id.org/engrd/connector/";
	private Boolean encodePayload = false;
	private Connector baseConnector;

	@BeforeEach
	public void init() {

		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(artifactMessageHandler, "issuerConnector", issuerConnector);
		ReflectionTestUtils.setField(artifactMessageHandler, "encodePayload", encodePayload);
		message = UtilMessageService.getArtifactRequestMessage();
		baseConnector = SelfDescriptionUtil.createDefaultSelfDescription();

	}

	@Test
	void handleMessageTest() {
		when(selfDescriptionService.getSelfDescription(message)).thenReturn(baseConnector);
		when(selfDescriptionService.artifactRequestedElementExist((ArtifactRequestMessage) message,
				selfDescriptionService.getSelfDescription(message))).thenReturn(true);

		responseMap = artifactMessageHandler.handleMessage(message, "asdsad");

		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}

	@Test
	void handleMessageEncodedPayloadTest() {
		ReflectionTestUtils.setField(artifactMessageHandler, "encodePayload", true);
		when(selfDescriptionService.getSelfDescription(message)).thenReturn(baseConnector);
		when(selfDescriptionService.artifactRequestedElementExist((ArtifactRequestMessage) message,
				selfDescriptionService.getSelfDescription(message))).thenReturn(true);

		responseMap = artifactMessageHandler.handleMessage(message, "asdsad" );
		
		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}

	@Test
	void handleMessageBigPayloadTest() throws URISyntaxException {
		ReflectionTestUtils.setField(artifactMessageHandler, "encodePayload", true);
		when(selfDescriptionService.getSelfDescription(message)).thenReturn(baseConnector);
		when(selfDescriptionService.artifactRequestedElementExist((ArtifactRequestMessage) message,
				selfDescriptionService.getSelfDescription(message))).thenReturn(true);
		
		ArtifactRequestMessage arm = (ArtifactRequestMessage) message;
		arm.setRequestedArtifact(new URI("http://w3id.org/engrd/connector/artifact/big"));
		responseMap = artifactMessageHandler.handleMessage(arm, "asdsad");
		
		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}

	@Test
	void handleMessageBigPayloadEndodedTest() throws URISyntaxException {
		
		when(selfDescriptionService.getSelfDescription(message)).thenReturn(baseConnector);
		when(selfDescriptionService.artifactRequestedElementExist((ArtifactRequestMessage) message,
				selfDescriptionService.getSelfDescription(message))).thenReturn(true);
		
		ArtifactRequestMessage arm = (ArtifactRequestMessage) message;
		arm.setRequestedArtifact(new URI("http://w3id.org/engrd/connector/artifact/big"));
		responseMap = artifactMessageHandler.handleMessage(arm, "asdsad");
		
		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}
	
	@Test
	void handleMessageTestRequestedArtifactNull() {

		ArtifactRequestMessage arm = (ArtifactRequestMessage) message;
		arm.setRequestedArtifact(null);

		BadParametersException exception = assertThrows(BadParametersException.class, () -> {
			responseMap = artifactMessageHandler.handleMessage(message, "asdsad");
		});

		assertEquals("Artifact requestedElement not provided", exception.getMessage());
	}

	@Test
	void handleMessageRequestElementNotPresentInSelfDescription() {
		
		when(selfDescriptionService.getSelfDescription(message)).thenReturn(baseConnector);
		when(selfDescriptionService.artifactRequestedElementExist((ArtifactRequestMessage) message,selfDescriptionService.getSelfDescription(message))).thenReturn(false);

		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			responseMap = artifactMessageHandler.handleMessage(message, "asdsad");
		});
		assertEquals("Artifact requestedElement not found in self description", exception.getMessage());
	}
}

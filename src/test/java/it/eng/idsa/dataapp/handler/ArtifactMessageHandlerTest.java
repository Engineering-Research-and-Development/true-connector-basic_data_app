package it.eng.idsa.dataapp.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.util.UtilMessageService;

class ArtifactMessageHandlerTest {

	@InjectMocks
	private ArtifactMessageHandler artifactMessageHandler;
	private Message message;
	Map<String, Object> responseMap = new HashMap<>();
	private String issuerConnector = "http://w3id.org/engrd/connector/";

	@BeforeEach
	public void init() {

		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(artifactMessageHandler, "issuerConnector", issuerConnector);
		message = UtilMessageService.getArtifactRequestMessage();
	}

	@Test
	void handleMessageTest() {

		responseMap = artifactMessageHandler.handleMessage(message, "asdsad");

		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}
}

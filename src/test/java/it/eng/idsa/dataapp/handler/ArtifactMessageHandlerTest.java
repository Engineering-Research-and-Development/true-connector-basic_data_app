package it.eng.idsa.dataapp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.service.CheckSumService;
import it.eng.idsa.dataapp.service.SelfDescriptionService;
import it.eng.idsa.dataapp.service.ThreadService;
import it.eng.idsa.dataapp.web.rest.exceptions.BadParametersException;
import it.eng.idsa.dataapp.web.rest.exceptions.NotFoundException;
import it.eng.idsa.multipart.processor.util.SelfDescriptionUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

class ArtifactMessageHandlerTest {

	private static final URI ARTIFACT_BIG = URI.create("http://w3id.org/engrd/connector/artifact/big");
	private ArtifactMessageHandler artifactMessageHandler;
	@Mock
	private SelfDescriptionService selfDescriptionService;
	@Mock
	private ThreadService threadService;
	@Mock
	private CheckSumService checkSumService;
	private Message message;
	Map<String, Object> responseMap = new HashMap<>();
	private String issuerConnector = "http://w3id.org/engrd/connector/";
	private Boolean encodePayload = false;
	private Connector baseConnector;
	static final String PAYLOAD = "asdsad";
	private Boolean contractNegotiationDemo = false;
	private Path dataLakeDirectory = Path.of("src/test/resources/dataFiles");
	private Boolean verifyCheckSum = false;

	@BeforeEach
	public void init() {

		MockitoAnnotations.openMocks(this);
		Optional<CheckSumService> optionalCheckSumService = Optional.of(checkSumService);

		artifactMessageHandler = new ArtifactMessageHandler(selfDescriptionService, threadService,
				optionalCheckSumService, dataLakeDirectory, verifyCheckSum, contractNegotiationDemo, encodePayload);
		ReflectionTestUtils.setField(artifactMessageHandler, "issuerConnector", issuerConnector);
		message = UtilMessageService.getArtifactRequestMessage();
		baseConnector = SelfDescriptionUtil.createDefaultSelfDescription();
		when(selfDescriptionService.getSelfDescription(message)).thenReturn(baseConnector);
		when(selfDescriptionService.artifactRequestedElementExist((ArtifactRequestMessage) message, baseConnector))
				.thenReturn(true);
	}

	@Test
	void handleMessageRestTest() {

		responseMap = artifactMessageHandler.handleMessage(message, PAYLOAD);

		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}

	@Test
	void handleMessageWssTest() {

		when(threadService.getThreadLocalValue("wss")).thenReturn(true);
		
		responseMap = artifactMessageHandler.handleMessage(message, "");

		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}

	@Test
	void handleMessageWssDemoTest() {

		ReflectionTestUtils.setField(artifactMessageHandler, "contractNegotiationDemo", true);

		when(threadService.getThreadLocalValue("wss")).thenReturn(true);

		responseMap = artifactMessageHandler.handleMessage(message, "");

		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}

	@Test
	void handleMessageEncodedPayloadRestTest() {
		ReflectionTestUtils.setField(artifactMessageHandler, "encodePayload", true);

		responseMap = artifactMessageHandler.handleMessage(message, PAYLOAD);

		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}

	@Test
	void handleMessageBigPayloadRestTest() throws URISyntaxException {
		ReflectionTestUtils.setField(artifactMessageHandler, "encodePayload", true);

		ArtifactRequestMessage arm = (ArtifactRequestMessage) message;
		arm.setRequestedArtifact(ARTIFACT_BIG);
		responseMap = artifactMessageHandler.handleMessage(arm, PAYLOAD);

		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get(DataAppMessageHandler.HEADER).toString(),
				message.getId().toString()));
	}

	@Test
	void handleMessageBigPayloadEndodedRestTest() throws URISyntaxException {

		ArtifactRequestMessage arm = (ArtifactRequestMessage) message;
		arm.setRequestedArtifact(ARTIFACT_BIG);
		responseMap = artifactMessageHandler.handleMessage(arm, PAYLOAD);

		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get(DataAppMessageHandler.HEADER).toString(),
				message.getId().toString()));
	}

	@Test
	void handleMessageContractNegotiationDemoRestTest() throws URISyntaxException {

		ReflectionTestUtils.setField(artifactMessageHandler, "contractNegotiationDemo", true);
		ReflectionTestUtils.setField(artifactMessageHandler, "encodePayload", true);

		ArtifactRequestMessage arm = (ArtifactRequestMessage) message;
		arm.setRequestedArtifact(UtilMessageService.REQUESTED_ARTIFACT);
		responseMap = artifactMessageHandler.handleMessage(arm, PAYLOAD);

		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get(DataAppMessageHandler.HEADER).toString(),
				message.getId().toString()));
	}

	@Test
	void handleMessageTestRequestedArtifactNullTest() {

		ArtifactRequestMessage arm = (ArtifactRequestMessage) message;
		arm.setRequestedArtifact(null);

		BadParametersException exception = assertThrows(BadParametersException.class, () -> {
			responseMap = artifactMessageHandler.handleMessage(message, PAYLOAD);
		});

		assertEquals("Artifact requestedElement not provided", exception.getMessage());
	}

	@Test
	void handleMessageRequestElementNotPresentInSelfDescriptionRestTest() {
		
		when(selfDescriptionService.artifactRequestedElementExist((ArtifactRequestMessage) message,selfDescriptionService.getSelfDescription(message))).thenReturn(false);

		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			responseMap = artifactMessageHandler.handleMessage(message, PAYLOAD);
		});
		assertEquals("Artifact requestedElement not found in self description", exception.getMessage());
	}

	@Test
	void handleMessageRequestElementNotPresentInSelfDescriptionWssTest() {
		
		when(threadService.getThreadLocalValue("wss")).thenReturn(true);

		when(selfDescriptionService.artifactRequestedElementExist((ArtifactRequestMessage) message,
				selfDescriptionService.getSelfDescription(message))).thenReturn(false);

		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			responseMap = artifactMessageHandler.handleMessage(message, "");
		});
		assertEquals("Artifact requestedElement not found in self description", exception.getMessage());
	}

	@Test
	void handleMessageNotReadFileWssTest() {

		ArtifactRequestMessage arm = (ArtifactRequestMessage) message;
		arm.setRequestedArtifact(URI.create("http://w3id.org/engrd/connector/artifact/123"));
		when(threadService.getThreadLocalValue("wss")).thenReturn(true);

		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			responseMap = artifactMessageHandler.handleMessage(arm, "");
		});
		assertEquals("Could't read the file from datalake", exception.getMessage());
	}
}

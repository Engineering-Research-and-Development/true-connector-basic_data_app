package it.eng.idsa.dataapp.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.gson.GsonBuilder;

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

		responseMap = artifactMessageHandler.handleMessage(message, "asdsad" );
		
		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}

	@Test
	void handleMessageBigPayloadTest() throws URISyntaxException {
		
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

	@Test
	public void isBigPayloadTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		String pathWithBig = "http://w3id.org/engrd/connector/artifact/big";
		String pathWithoutBig = "http://w3id.org/engrd/connector/artifact/1";
		Method isBigPayload = ArtifactMessageHandler.class.getDeclaredMethod("isBigPayload", String.class);
		isBigPayload.setAccessible(true);

		assertTrue((boolean) isBigPayload.invoke(artifactMessageHandler, pathWithBig));
		assertFalse((boolean) isBigPayload.invoke(artifactMessageHandler, pathWithoutBig));
	}

	@Test
	public void createResponsePayloadTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Method createResponsePayload = ArtifactMessageHandler.class.getDeclaredMethod("createResponsePayload");
		createResponsePayload.setAccessible(true);

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String formattedDate = dateFormat.format(date);

		Map<String, String> expectedPayload = new HashMap<>();
		expectedPayload.put("firstName", "John");
		expectedPayload.put("lastName", "Doe");
		expectedPayload.put("dateOfBirth", formattedDate);
		expectedPayload.put("address", "591  Franklin Street, Pennsylvania");
		expectedPayload.put("checksum", "ABC123 " + formattedDate);

		String expectedJson = new GsonBuilder().create().toJson(expectedPayload);
		String actualJson = (String) createResponsePayload.invoke(artifactMessageHandler);

		assertEquals(expectedJson, actualJson);
	}

	@Test
	public void encodePayloadTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Method encodePayload = ArtifactMessageHandler.class.getDeclaredMethod("encodePayload", byte[].class);
		encodePayload.setAccessible(true);

		byte[] payload = { 0x01, 0x02, 0x03 };

		assertNotNull((String) encodePayload.invoke(artifactMessageHandler, payload));
	}
}

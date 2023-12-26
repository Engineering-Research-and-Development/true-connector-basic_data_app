package it.eng.idsa.dataapp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.service.SelfDescriptionService;
import it.eng.idsa.dataapp.web.rest.exceptions.BadParametersException;
import it.eng.idsa.dataapp.web.rest.exceptions.InternalRecipientException;
import it.eng.idsa.dataapp.web.rest.exceptions.NotFoundException;
import it.eng.idsa.multipart.processor.util.SelfDescriptionUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

class ContractRequestMessageHandlerTest {
	@InjectMocks
	private ContractRequestMessageHandler contractRequestMessageHandler;
	@Mock
	private ResponseEntity<String> response;
	@Mock
	private SelfDescriptionService selfDescriptionService;
	private Message message;
	Map<String, Object> responseMap = new HashMap<>();
	private String issuerConnector = "http://w3id.org/engrd/connector/";
	private Boolean contractNegotiationDemo = true;
	private String usageControlVersionPlatoon = "platoon";
	private String usageControlVersionMyData = "mydata";
	private Path dataLakeDirectory;
	private Connector baseConnector;
	private Serializer serializer = new Serializer();

	@BeforeEach
	public void init() throws IOException, URISyntaxException {

		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(contractRequestMessageHandler, "issuerConnector", issuerConnector);
		ReflectionTestUtils.setField(contractRequestMessageHandler, "contractNegotiationDemo", contractNegotiationDemo);
		baseConnector = SelfDescriptionUtil.createDefaultSelfDescription();
		message = UtilMessageService.getContractRequestMessage();
	}

	@Test
	void handleMessageTest() {

		responseMap = contractRequestMessageHandler.handleMessage(message, "asdsad");

		assertNotNull(responseMap.get("header"));
		assertNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}

	@Test
	void handleMessagePlatoonNotFoundExceptionTest() throws IOException {
		String contractAgreement = serializer.serialize(UtilMessageService
				.getContractRequest(URI.create("https://artifact.id"), URI.create("https://permission.id")));

		ReflectionTestUtils.setField(contractRequestMessageHandler, "usageControlVersion", usageControlVersionPlatoon);
		when(selfDescriptionService.getSelfDescription(message)).thenReturn(baseConnector);
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			responseMap = contractRequestMessageHandler.handleMessage(message, contractAgreement);
		});

		assertEquals("Could not find contract offer that match with request - permissionId and target",
				exception.getMessage());
	}

	@Test
	void handleMessagePlatoonInvalidPayloadTest() {

		ReflectionTestUtils.setField(contractRequestMessageHandler, "usageControlVersion", usageControlVersionPlatoon);

		InternalRecipientException exception = assertThrows(InternalRecipientException.class, () -> {
			responseMap = contractRequestMessageHandler.handleMessage(message, "asdasdasd");
		});

		assertEquals("Error while creating contract agreement", exception.getMessage());
	}

	@Test
	void handleMessagePlatoonSelfDescriptionNullTest() {

		ReflectionTestUtils.setField(contractRequestMessageHandler, "usageControlVersion", usageControlVersionPlatoon);

		BadParametersException exception = assertThrows(BadParametersException.class, () -> {
			responseMap = contractRequestMessageHandler.handleMessage(message, null);
		});

		assertEquals("Payload is null", exception.getMessage());
	}

	@Test
	void handleMessageMyDataTest() {

		dataLakeDirectory = Paths.get("src/main/resources/dataFiles");

		ReflectionTestUtils.setField(contractRequestMessageHandler, "usageControlVersion", usageControlVersionMyData);
		ReflectionTestUtils.setField(contractRequestMessageHandler, "dataLakeDirectory", dataLakeDirectory);

		responseMap = contractRequestMessageHandler.handleMessage(message, "asdsad");

		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}

	@Test
	void handleMessageMyDataExceptionTest() {
		dataLakeDirectory = Paths.get("/");

		ReflectionTestUtils.setField(contractRequestMessageHandler, "usageControlVersion", usageControlVersionMyData);
		ReflectionTestUtils.setField(contractRequestMessageHandler, "dataLakeDirectory", dataLakeDirectory);

		InternalRecipientException exception = assertThrows(InternalRecipientException.class, () -> {
			responseMap = contractRequestMessageHandler.handleMessage(message, "asdasdasd");
		});

		assertEquals("Error while reading contract agreement file from dataLakeDirectory", exception.getMessage());
	}
}

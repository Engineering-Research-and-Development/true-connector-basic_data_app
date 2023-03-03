package it.eng.idsa.dataapp.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
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

	@BeforeEach
	public void init() throws IOException, URISyntaxException {

		MockitoAnnotations.initMocks(this);

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
	void handleMessageWithoutContractNegotiationExceptionTest() {
		ReflectionTestUtils.setField(contractRequestMessageHandler, "contractNegotiationDemo", false);

		InternalRecipientException exception = assertThrows(InternalRecipientException.class, () -> {
			responseMap = contractRequestMessageHandler.handleMessage(message, "asdasdasd");
		});

		assertEquals("Creating processed notification, contract agreement needs evaluation", exception.getMessage());
	}

	@Test
	void handleMessagePlatoonNotFoundExceptionTest() {

		ReflectionTestUtils.setField(contractRequestMessageHandler, "usageControlVersion", usageControlVersionPlatoon);
		when(selfDescriptionService.getSelfDescription(message)).thenReturn(baseConnector);
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			responseMap = contractRequestMessageHandler.handleMessage(message,
					"{\"ids:consumer\":{\"@id\":\"http://w3id.org/engrd/connector/consumer\"},\"@type\":\"ids:ContractRequest\",\"ids:provider\":{\"@id\":\"https://w3id.org/engrd/connector/\"},\"ids:permission\":[{\"ids:postDuty\":[],\"ids:action\":[{\"@id\":\"https://w3id.org/idsa/code/USE\"}],\"ids:constraint\":[],\"ids:assignee\":[],\"@type\":\"ids:Permission\",\"ids:title\":[{\"@value\":\"Example Usage Policy\",\"@type\":\"http://www.w3.org/2001/XMLSchema#string\"}],\"ids:preDuty\":[],\"ids:description\":[{\"@value\":\"provide-access\",\"@type\":\"http://www.w3.org/2001/XMLSchema#string\"}],\"ids:target\":{\"@id\":\"http://w3id.org/engrd/connector/artifact/1\"},\"@id\":\"https://w3id.org/idsa/autogen/permission/5c5b8374-2597-4c5e-b694-494ccaa68136\",\"ids:assigner\":[]}],\"@id\":\"https://w3id.org/idsa/autogen/contractOffer/008c7957-1e12-4538-9ba0-22d49216e578\",\"ids:prohibition\":[],\"ids:obligation\":[],\"@context\":{\"ids\":\"https://w3id.org/idsa/core/\",\"idsc\":\"https://w3id.org/idsa/code/\"}}");
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

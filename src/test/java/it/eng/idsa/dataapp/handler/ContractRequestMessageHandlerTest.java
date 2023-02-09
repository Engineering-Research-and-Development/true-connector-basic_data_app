package it.eng.idsa.dataapp.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
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

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.service.SelfDescriptionService;
import it.eng.idsa.dataapp.web.rest.exceptions.BadParametersException;
import it.eng.idsa.dataapp.web.rest.exceptions.InternalRecipientException;
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
	private String usageControlVersion = "platoon";

	@BeforeEach
	public void init() throws IOException, URISyntaxException {

		MockitoAnnotations.initMocks(this);

		ReflectionTestUtils.setField(contractRequestMessageHandler, "issuerConnector", issuerConnector);
		ReflectionTestUtils.setField(contractRequestMessageHandler, "contractNegotiationDemo", contractNegotiationDemo);
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
	void handleMessagePlatoonInvalidPayloadTest() {

		ReflectionTestUtils.setField(contractRequestMessageHandler, "usageControlVersion", usageControlVersion);

		InternalRecipientException exception = assertThrows(InternalRecipientException.class, () -> {
			responseMap = contractRequestMessageHandler.handleMessage(message, "asdasdasd");
		});

		assertEquals("Error while creating contract agreement", exception.getMessage());
	}

	@Test
	void handleMessagePlatoonSelfDescriptionNullTest() {

		ReflectionTestUtils.setField(contractRequestMessageHandler, "usageControlVersion", usageControlVersion);

		BadParametersException exception = assertThrows(BadParametersException.class, () -> {
			responseMap = contractRequestMessageHandler.handleMessage(message, null);
		});

		assertEquals("Payload is null", exception.getMessage());
	}
}

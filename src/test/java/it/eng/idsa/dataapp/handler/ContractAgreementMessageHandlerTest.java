package it.eng.idsa.dataapp.handler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

class ContractAgreementMessageHandlerTest {

	@InjectMocks
	private ContractAgreementMessageHandler agreementMessageHandler;
	private Message message;
	Map<String, Object> responseMap = new HashMap<>();
	private String issuerConnector = "http://w3id.org/engrd/connector/";

	@BeforeEach
	public void init() {

		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(agreementMessageHandler, "issuerConnector", issuerConnector);
		message = UtilMessageService.getContractAgreementMessage();
	}

	@Test
	void handleMessageTest() {

		responseMap = agreementMessageHandler.handleMessage(message, "asdsad");

		assertNotNull(responseMap.get("header"));
		assertNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}

	@Test
	void handleMessageHeaderNullTest() {

		responseMap = agreementMessageHandler.handleMessage(null, null);

		assertNotNull(responseMap.get("header"));
		assertNull(responseMap.get("payload"));
	}
}

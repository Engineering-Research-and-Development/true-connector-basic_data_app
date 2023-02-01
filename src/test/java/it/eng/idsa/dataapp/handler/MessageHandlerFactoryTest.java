package it.eng.idsa.dataapp.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.util.UtilMessageService;

class MessageHandlerFactoryTest {

	@Mock
	private MessageHandlerFactory factory;
	@Mock
	private DescriptionRequestMessageHandler descriptionRequestMessageHandler;
	@Mock
	private ContractRequestMessageHandler contractRequestMessageHandler;
	@Mock
	private ContractAgreementMessageHandler contractAgreementMessageHandler;
	@Mock
	private ArtifactMessageHandler artifactMessageHandler;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void createMessageHandler_DescriptionRequestMessage() {
		Message message = UtilMessageService.getDescriptionRequestMessage(null);

		when(factory.createMessageHandler(message.getClass())).thenReturn(descriptionRequestMessageHandler);

		DataAppMessageHandler handler = factory.createMessageHandler(message.getClass());

		assertNotNull(handler);
		assertTrue(handler instanceof DescriptionRequestMessageHandler);
	}

	@Test
	void createMessageHandler_ContractRequestMessageHandler() {
		Message message = UtilMessageService.getContractRequestMessage();

		when(factory.createMessageHandler(message.getClass())).thenReturn(contractRequestMessageHandler);

		DataAppMessageHandler handler = factory.createMessageHandler(message.getClass());

		assertNotNull(handler);
		assertTrue(handler instanceof ContractRequestMessageHandler);
	}

	@Test
	void createMessageHandler_ContractAgreementMessageHandler() {
		Message message = UtilMessageService.getContractAgreementMessage();

		when(factory.createMessageHandler(message.getClass())).thenReturn(contractAgreementMessageHandler);

		DataAppMessageHandler handler = factory.createMessageHandler(message.getClass());

		assertNotNull(handler);
		assertTrue(handler instanceof ContractAgreementMessageHandler);
	}

	@Test
	void createMessageHandler_ArtifactMessageHandler() {
		Message message = UtilMessageService.getArtifactRequestMessage();

		when(factory.createMessageHandler(message.getClass())).thenReturn(artifactMessageHandler);

		DataAppMessageHandler handler = factory.createMessageHandler(message.getClass());

		assertNotNull(handler);
		assertTrue(handler instanceof ArtifactMessageHandler);
	}
}

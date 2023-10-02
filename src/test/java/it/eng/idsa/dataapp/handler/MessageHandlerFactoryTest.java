package it.eng.idsa.dataapp.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import de.fraunhofer.iais.eis.ArtifactRequestMessageImpl;
import de.fraunhofer.iais.eis.ContractAgreementMessageImpl;
import de.fraunhofer.iais.eis.ContractRequestMessageImpl;
import de.fraunhofer.iais.eis.DescriptionRequestMessageImpl;

class MessageHandlerFactoryTest {

	@InjectMocks
	private MessageHandlerFactory factory;
	@Mock
	private DescriptionRequestMessageHandler descriptionRequestMessageHandler;
	@Mock
	private ContractRequestMessageHandler contractRequestMessageHandler;
	@Mock
	private ContractAgreementMessageHandler contractAgreementMessageHandler;
	@Mock
	private ArtifactMessageHandler artifactMessageHandler;

	@Mock
	private ApplicationContext context;

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
    public void testCreateMessageHandler() {
		
 	   when(context.getBean(ArtifactMessageHandler.class)).thenReturn(artifactMessageHandler);
       when(context.getBean(DescriptionRequestMessageHandler.class)).thenReturn(descriptionRequestMessageHandler);
       when(context.getBean(ContractRequestMessageHandler.class)).thenReturn(contractRequestMessageHandler);
       when(context.getBean(ContractAgreementMessageHandler.class)).thenReturn(contractAgreementMessageHandler);
       
       DataAppMessageHandler artifactRequestHandler = factory.createMessageHandler(ArtifactRequestMessageImpl.class);
       DataAppMessageHandler descriptionRequestHandler = factory.createMessageHandler(DescriptionRequestMessageImpl.class);
       DataAppMessageHandler contractRequestHandler = factory.createMessageHandler(ContractRequestMessageImpl.class);
       DataAppMessageHandler contractAgreementHandler = factory.createMessageHandler(ContractAgreementMessageImpl.class);
       
       assertEquals(artifactMessageHandler, artifactRequestHandler);
       assertEquals(descriptionRequestMessageHandler, descriptionRequestHandler);
       assertEquals(contractRequestMessageHandler, contractRequestHandler);
       assertEquals(contractAgreementMessageHandler, contractAgreementHandler);
    }
}

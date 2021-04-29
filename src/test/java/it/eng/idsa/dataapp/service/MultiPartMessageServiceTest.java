package it.eng.idsa.dataapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ResultMessage;
import it.eng.idsa.dataapp.service.impl.MultiPartMessageServiceImpl;
import it.eng.idsa.multipart.util.TestUtilMessageService;

public class MultiPartMessageServiceTest {

	private static final String INFO_MODEL_VERSION = "4.0.6";
	private MultiPartMessageService service = new MultiPartMessageServiceImpl(INFO_MODEL_VERSION);
	
	@Test
	public void createArtifactResponseMessage() {
		Message message = service.createArtifactResponseMessage(TestUtilMessageService.getArtifactRequestMessage());
		assertNotNull(message);
		assertTrue(message instanceof ArtifactResponseMessage);
	}
	
	@Test
	public void createContractAgreementMessage() {
		Message message = service.createContractAgreementMessage(TestUtilMessageService.getContractRequestMessage());
		assertNotNull(message);
		assertTrue(message instanceof ContractAgreementMessage);
	}
	
	@Test
	public void createResultMessage() {
		Message message = service.createResultMessage(TestUtilMessageService.getArtifactRequestMessage());
		assertNotNull(message);
		assertTrue(message instanceof ResultMessage);
	}
	
	@Test
	public void getToken() {
		String token = service.getToken(TestUtilMessageService.getMessageAsString(TestUtilMessageService.getArtifactRequestMessage()));
		assertNotNull(token);
		assertEquals(TestUtilMessageService.TOKEN_VALUE, token);
	}
}

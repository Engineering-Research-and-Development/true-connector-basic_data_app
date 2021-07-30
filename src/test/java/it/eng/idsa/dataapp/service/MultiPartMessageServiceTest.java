package it.eng.idsa.dataapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;

import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ResultMessage;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.service.impl.MultiPartMessageServiceImpl;
import it.eng.idsa.multipart.util.UtilMessageService;

public class MultiPartMessageServiceTest {

	private static final String INFO_MODEL_VERSION = "4.1.1";
	private MultiPartMessageService service = new MultiPartMessageServiceImpl(INFO_MODEL_VERSION);
	// Added serializer since URI fields must be proper URI's not auto-generated
	private Serializer serializer = new Serializer();
	
	@Test
	public void createArtifactResponseMessage() throws IOException {
		Message message = service.createArtifactResponseMessage(UtilMessageService.getArtifactRequestMessage(URI.create("http://some.artifact.com")));
		assertNotNull(message);
		assertTrue(message instanceof ArtifactResponseMessage);
		serializer.serialize(message);
	}
	
	@Test
	public void createContractAgreementMessage() throws IOException {
		Message message = service.createContractAgreementMessage(UtilMessageService.getContractRequestMessage());
		assertNotNull(message);
		assertTrue(message instanceof ContractAgreementMessage);
		serializer.serialize(message);
	}
	
	@Test
	public void createResultMessage() throws IOException {
		Message message = service.createResultMessage(UtilMessageService.getArtifactRequestMessage());
		assertNotNull(message);
		assertTrue(message instanceof ResultMessage);
		serializer.serialize(message);
	}
	
	@Test
	public void getToken() throws IOException {
		String token = service.getToken(UtilMessageService.getMessageAsString(UtilMessageService.getArtifactRequestMessage()));
		assertNotNull(token);
		assertEquals(UtilMessageService.TOKEN_VALUE, token);
		serializer.serialize(token);
	}
}

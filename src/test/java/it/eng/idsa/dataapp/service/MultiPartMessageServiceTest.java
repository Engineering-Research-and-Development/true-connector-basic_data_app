package it.eng.idsa.dataapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ResultMessage;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.service.impl.MultiPartMessageServiceImpl;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.TestUtilMessageService;

public class MultiPartMessageServiceTest {

	private static final String INFO_MODEL_VERSION = "4.0.6";
	private MultiPartMessageService service = new MultiPartMessageServiceImpl(INFO_MODEL_VERSION);
	// Added serializer since URI fields must be proper URI's not auto-generated
	private Serializer serializer = new Serializer();
	
	@Test
	public void createArtifactResponseMessage() throws IOException {
		Message message = service.createArtifactResponseMessage(TestUtilMessageService.getArtifactRequestMessage());
		assertNotNull(message);
		assertTrue(message instanceof ArtifactResponseMessage);
		serializer.serialize(message);
	}
	
	@Test
	public void createContractAgreementMessage() throws IOException {
		Message message = service.createContractAgreementMessage(TestUtilMessageService.getContractRequestMessage());
		assertNotNull(message);
		assertTrue(message instanceof ContractAgreementMessage);
		serializer.serialize(message);
	}
	
	@Test
	public void createResultMessage() throws IOException {
		Message message = service.createResultMessage(TestUtilMessageService.getArtifactRequestMessage());
		assertNotNull(message);
		assertTrue(message instanceof ResultMessage);
		serializer.serialize(message);
	}
	
	@Test
	public void getToken() throws IOException {
		String token = service.getToken(TestUtilMessageService.getMessageAsString(TestUtilMessageService.getArtifactRequestMessage()));
		assertNotNull(token);
		assertEquals(TestUtilMessageService.TOKEN_VALUE, token);
		serializer.serialize(token);
	}
}

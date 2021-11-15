package it.eng.idsa.dataapp.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.ResultMessage;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.multipart.processor.util.SelfDescriptionUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

public class MessageUtilTest {
	
	private MessageUtil messageUtil;
	
	@Mock
	private Path dataLakeDirectory;
	
	@Mock
	private RestTemplate restTemplate;
	
	@Mock
	private ECCProperties eccProperties;
	
	private HttpHeaders headers;
	
	private Serializer serializer = new Serializer();
	private Connector baseConnector;
	
	private static final String IDS_MESSAGE_TYPE = "IDS-MessageType";
	private static final String IDS_REQUESTED_ELEMENT = "IDS-RequestedElement";
	private static final URI EXISTING_REQUESTED_ELEMENT_ID = SelfDescriptionUtil.getBaseConnector().getResourceCatalog().get(0).getOfferedResource().get(0).getId();
	private static final URI NON_EXISTING_REQUESTED_ELEMENT_ID = URI.create(EXISTING_REQUESTED_ELEMENT_ID + "NonExistingElement");
	
	@BeforeEach
	public void init() throws RestClientException, IOException {
		MockitoAnnotations.initMocks(this);
		when(eccProperties.getHost()).thenReturn("localhost");
		baseConnector = SelfDescriptionUtil.getBaseConnector();
		String selfDescriptionAsString = serializer.serialize(baseConnector);
		when(restTemplate.getForObject(any(), any())).thenReturn(selfDescriptionAsString);
		dataLakeDirectory = Path.of("src", "test", "resources");
		messageUtil = new MessageUtil(dataLakeDirectory, restTemplate, eccProperties);
		headers = new HttpHeaders();
	}
	
	//Description request message without requested element
	//Description request message as Java Object
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHeaderMessageSuccessfull() throws IOException {
 		String payload = messageUtil.createResponsePayload(UtilMessageService.getDescriptionRequestMessage(null));
 		assertTrue(payload.contains(baseConnector.getId().toString()));
	}
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHeaderMessageFailed() {
		when(restTemplate.getForObject(any(), any())).thenReturn(null);
 		assertThrows(NullPointerException.class, () -> messageUtil.createResponsePayload(UtilMessageService.getDescriptionRequestMessage(null)));
	}
	
	//Description request message as String
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHeaderStringSuccessfull() throws IOException {
 		String payload = messageUtil.createResponsePayload(UtilMessageService.getDescriptionRequestMessage(null));
 		assertTrue(payload.contains(baseConnector.getId().toString()));
	}
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHeaderStringFailed() {
		when(restTemplate.getForObject(any(), any())).thenReturn(null);
		assertThrows(NullPointerException.class, () -> messageUtil.createResponsePayload(UtilMessageService.getDescriptionRequestMessage(null)));
	}
	
	//Description request message in Http Headers
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHttpHeadersSuccessfull() throws IOException {
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
 		String payload = messageUtil.createResponsePayload(headers);
 		assertTrue(payload.contains(baseConnector.getId().toString()));
	}
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHttpHeadersFailed() {
		when(restTemplate.getForObject(any(), any())).thenReturn(null);
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
		assertThrows(NullPointerException.class, () -> messageUtil.createResponsePayload(headers));
	}
	
	//Description request message with requested element
	//Description request message as Java Object
	
	@Test
	public void testResponsePayloadWithRequestedElementInHeaderMessageFailed() throws IOException {
 		String payload = messageUtil.createResponsePayload(UtilMessageService.getDescriptionRequestMessage(NON_EXISTING_REQUESTED_ELEMENT_ID));
		assertTrue(payload.contains(RejectionMessage.class.getSimpleName()));
	}
	
	//Description request message as String
	
	@Test
	public void testResponsePayloadWithRequestedElementInHeaderStringSuccessfull() throws IOException {
		DescriptionRequestMessage drm = UtilMessageService.getDescriptionRequestMessage(EXISTING_REQUESTED_ELEMENT_ID);
 		String payload = messageUtil.createResponsePayload(drm);
 		assertTrue(payload.contains(EXISTING_REQUESTED_ELEMENT_ID.toString()));
	}
	
	@Test
	public void testResponsePayloadWithRequestedElementInHeaderStringFailed() throws IOException {
		DescriptionRequestMessage drm = UtilMessageService.getDescriptionRequestMessage(NON_EXISTING_REQUESTED_ELEMENT_ID);
 		String payload = messageUtil.createResponsePayload(drm);
 		assertTrue(payload.contains(RejectionMessage.class.getSimpleName()));
	}
	
	//Description request message in Http Headers
	
	@Test
	public void testResponsePayloadWithRequestedElementInHttpHeadersSuccessfull() throws IOException {
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
		headers.add(IDS_REQUESTED_ELEMENT, EXISTING_REQUESTED_ELEMENT_ID.toString());
 		String payload = messageUtil.createResponsePayload(headers);
 		assertTrue(payload.contains(EXISTING_REQUESTED_ELEMENT_ID.toString()));
	}
	
	@Test
	public void testResponsePayloadWithRequestedElementInHttpHeadersFailed() {
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
		headers.add(IDS_REQUESTED_ELEMENT, NON_EXISTING_REQUESTED_ELEMENT_ID.toString());
 		String payload = messageUtil.createResponsePayload(headers);
		assertEquals("IDS-RejectionReason:https://w3id.org/idsa/code/NOT_FOUND", payload);
	}	
	
	//Response for ContractRequestMessage
	
	@Test
	public void testResponsePayload_IDSContractRequestMessage() throws IOException {
		String payload = messageUtil.createResponsePayload(UtilMessageService.getContractRequestMessage());
		assertTrue(payload.contains(ContractAgreement.class.getSimpleName()));
	}
	
	@Test
	public void testResponsePayload_StringContractRequestMessage() throws IOException {
		String payload = messageUtil.createResponsePayload(UtilMessageService.getContractRequestMessage());
		assertTrue(payload.contains(ContractAgreement.class.getSimpleName()));
	}
	
	//Response for ContractAgreementMessage
	
	@Test
	public void testResponsePayload_IDSContractAgreementMessage(){
		String payload = messageUtil.createResponsePayload(UtilMessageService.getContractAgreementMessage());
		assertNull(payload);
	}

	@Test
	public void testResponsePayload_StringContractAgreementMessage(){
		String payload = messageUtil.createResponsePayload(UtilMessageService.getContractAgreementMessage());
		assertNull(payload);;
	}
	
	//Response for ArtifactRequestMessage
	
	@Test
	public void testResponsePayload_IDSArtifactRequestMessage(){
		String payload = messageUtil.createResponsePayload(UtilMessageService.getArtifactRequestMessage());
		assertTrue(payload.contains("John"));
		assertTrue(payload.contains("Doe"));
		assertTrue(payload.contains("591  Franklin Street, Pennsylvania"));
	}

	@Test
	public void createArtifactResponseMessage() throws IOException {
		// provide default ArtifactRequestMessage so it does not fail on check for
		// transfer contract and requested element
		Message message = messageUtil.createArtifactResponseMessage(UtilMessageService.getArtifactRequestMessage());
		assertNotNull(message);
		assertTrue(message instanceof ArtifactResponseMessage);
		serializer.serialize(message);
	}

	@Test
	public void createContractAgreementMessage() throws IOException {
		Message message = messageUtil.createContractAgreementMessage(UtilMessageService.getContractRequestMessage());
		assertNotNull(message);
		assertTrue(message instanceof ContractAgreementMessage);
		serializer.serialize(message);
	}

	@Test
	public void createResultMessage() throws IOException {
		Message message = messageUtil.createResultMessage(UtilMessageService.getArtifactRequestMessage());
		assertNotNull(message);
		assertTrue(message instanceof ResultMessage);
		serializer.serialize(message);
	}
}

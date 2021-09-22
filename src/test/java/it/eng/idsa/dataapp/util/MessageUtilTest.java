package it.eng.idsa.dataapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.service.MultiPartMessageService;
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
	
	@Mock
	private MultiPartMessageService multiPartMessageService;
	
	private HttpHeaders headers;
	
	private Serializer serializer = new Serializer();
	
	private static final String IDS_MESSAGE_TYPE = "IDS-MessageType";
	private static final String IDS_REQUESTED_ELEMENT = "IDS-RequestedElement";
	private static final URI EXISTING_REQUESTED_ELEMENT_ID = SelfDescriptionUtil.getBaseConnector().getResourceCatalog().get(0).getOfferedResource().get(0).getId();
	private static final URI NON_EXISTING_REQUESTED_ELEMENT_ID = URI.create(EXISTING_REQUESTED_ELEMENT_ID + "NonExistingElement");
	
	@BeforeEach
	public void init() throws RestClientException, IOException {
		MockitoAnnotations.initMocks(this);
		when(eccProperties.getHost()).thenReturn("localhost");
		when(restTemplate.getForObject(any(), any())).thenReturn(serializer.serialize(SelfDescriptionUtil.getBaseConnector()));
		when(multiPartMessageService.createRejectionCommunicationLocalIssues(any())).thenReturn(UtilMessageService.getRejectionMessage(RejectionReason.NOT_FOUND));
		dataLakeDirectory = Path.of("/dataLakeDirectory");
		messageUtil = new MessageUtil(dataLakeDirectory, restTemplate, eccProperties, multiPartMessageService);
		headers = new HttpHeaders();
	}
	
	//Description request message without requested element
	//Description request message as Java Object
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHeaderMessageSuccessfull() throws IOException {
 		String payload = messageUtil.createResponsePayload(UtilMessageService.getDescriptionRequestMessage(null));
		assertEquals(SelfDescriptionUtil.getBaseConnector().getId(), serializer.deserialize(payload, Connector.class).getId());
	}
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHeaderMessageFailed() {
		when(restTemplate.getForObject(any(), any())).thenReturn(null);
 		assertThrows(NullPointerException.class, () -> messageUtil.createResponsePayload(UtilMessageService.getDescriptionRequestMessage(null)));
	}
	
	//Description request message as String
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHeaderStringSuccessfull() throws IOException {
 		String payload = messageUtil.createResponsePayload(UtilMessageService.getMessageAsString(UtilMessageService.getDescriptionRequestMessage(null)));
		assertEquals(SelfDescriptionUtil.getBaseConnector().getId(), serializer.deserialize(payload, Connector.class).getId());
	}
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHeaderStringFailed() {
		when(restTemplate.getForObject(any(), any())).thenReturn(null);
		assertThrows(NullPointerException.class, () -> messageUtil.createResponsePayload(UtilMessageService.getMessageAsString(UtilMessageService.getDescriptionRequestMessage(null))));
	}
	
	//Description request message in Http Headers
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHttpHeadersSuccessfull() throws IOException {
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
 		String payload = messageUtil.createResponsePayload(headers);
		assertEquals(SelfDescriptionUtil.getBaseConnector().getId(), serializer.deserialize(payload, Connector.class).getId());
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
	public void testResponsePayloadWithRequestedElementInHeaderMessageSuccessfull() throws IOException {
 		String payload = messageUtil.createResponsePayload(UtilMessageService.getDescriptionRequestMessage(EXISTING_REQUESTED_ELEMENT_ID));
		assertEquals(EXISTING_REQUESTED_ELEMENT_ID, serializer.deserialize(payload, Resource.class).getId());
	}
	
	@Test
	public void testResponsePayloadWithRequestedElementInHeaderMessageFailed() throws IOException {
 		String payload = messageUtil.createResponsePayload(UtilMessageService.getDescriptionRequestMessage(NON_EXISTING_REQUESTED_ELEMENT_ID));
		assertTrue(serializer.deserialize(payload, RejectionMessage.class) instanceof RejectionMessage);
	}
	
	//Description request message as String
	
	@Test
	public void testResponsePayloadWithRequestedElementInHeaderStringSuccessfull() throws IOException {
		DescriptionRequestMessage drm = UtilMessageService.getDescriptionRequestMessage(EXISTING_REQUESTED_ELEMENT_ID);
		String drmString = UtilMessageService.getMessageAsString(drm);
		when(multiPartMessageService.getMessage((Object)drmString)).thenReturn(drm);
 		String payload = messageUtil.createResponsePayload(drmString);
		assertEquals(EXISTING_REQUESTED_ELEMENT_ID, serializer.deserialize(payload, Resource.class).getId());
	}
	
	@Test
	public void testResponsePayloadWithRequestedElementInHeaderStringFailed() throws IOException {
		DescriptionRequestMessage drm = UtilMessageService.getDescriptionRequestMessage(NON_EXISTING_REQUESTED_ELEMENT_ID);
		String drmString = UtilMessageService.getMessageAsString(drm);
		when(multiPartMessageService.getMessage((Object)drmString)).thenReturn(drm);
 		String payload = messageUtil.createResponsePayload(drmString);
 		assertTrue(serializer.deserialize(payload, RejectionMessage.class) instanceof RejectionMessage);
	}
	
	//Description request message in Http Headers
	
	@Test
	public void testResponsePayloadWithRequestedElementInHttpHeadersSuccessfull() throws IOException {
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
		headers.add(IDS_REQUESTED_ELEMENT, EXISTING_REQUESTED_ELEMENT_ID.toString());
 		String payload = messageUtil.createResponsePayload(headers);
		assertEquals(EXISTING_REQUESTED_ELEMENT_ID, serializer.deserialize(payload, Resource.class).getId());
	}
	
	@Test
	public void testResponsePayloadWithRequestedElementInHttpHeadersFailed() {
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
		headers.add(IDS_REQUESTED_ELEMENT, NON_EXISTING_REQUESTED_ELEMENT_ID.toString());
 		String payload = messageUtil.createResponsePayload(headers);
		assertEquals("IDS-RejectionReason:https://w3id.org/idsa/code/NOT_FOUND", payload);
	}	
}

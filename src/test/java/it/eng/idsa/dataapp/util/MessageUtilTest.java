package it.eng.idsa.dataapp.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
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
	private RestTemplate restTemplate;
	
	@Mock
	private RestTemplateBuilder restTemplateBuilder;
	
	@Mock
	private ResponseEntity<String> response;
	
	@Mock
	private ECCProperties eccProperties;
	
	private HttpHeaders headers;
	
	private Serializer serializer = new Serializer();
	private Connector baseConnector;
	
	private static final String IDS_MESSAGE_TYPE = "IDS-MessageType";
	private static final String IDS_REQUESTED_ELEMENT = "IDS-RequestedElement";
	private String issuerConnector = "http://w3id.org/engrd/connector/";
	
	URI EXISTING_REQUESTED_ELEMENT_ID;
	URI NON_EXISTING_REQUESTED_ELEMENT_ID = URI.create(EXISTING_REQUESTED_ELEMENT_ID + "NonExistingElement");
	
	@BeforeEach
	public void init() throws RestClientException, IOException {
		MockitoAnnotations.initMocks(this);
		when(eccProperties.getHost()).thenReturn("fakeHost");
		when(eccProperties.getProtocol()).thenReturn("http");
		when(eccProperties.getPort()).thenReturn(1234);
		when(restTemplateBuilder.build()).thenReturn(restTemplate);
		baseConnector = SelfDescriptionUtil.createDefaultSelfDescription();
		EXISTING_REQUESTED_ELEMENT_ID = baseConnector.getResourceCatalog().get(0).getOfferedResource().get(0).getId();
		String selfDescriptionAsString = serializer.serialize(baseConnector);
		when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(response);
		when(response.getBody()).thenReturn(selfDescriptionAsString);
		when(response.getStatusCodeValue()).thenReturn(200);
		messageUtil = new MessageUtil(restTemplateBuilder, eccProperties, false, true, issuerConnector, "platoon", Path.of("."));
		//not most elegant way without setting default value
		ReflectionTestUtils.setField(messageUtil, "contractNegotiationDemo", true);
		headers = new HttpHeaders();
	}
	
	//Description request message without requested element
	//Description request message as Java Object
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHeaderMessageSuccessfull()  {
 		String payload = messageUtil.createResponsePayload(UtilMessageService.getDescriptionRequestMessage(null), "ABC");
 		assertTrue(payload.contains(baseConnector.getId().toString()));
	}
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHeaderMessageFailed() {
		when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(null);
		assertNull(messageUtil.createResponsePayload(UtilMessageService.getDescriptionRequestMessage(null), "ABC"));
	}
	
	//Description request message as String
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHeaderStringSuccessfull()  {
 		String payload = messageUtil.createResponsePayload(UtilMessageService.getDescriptionRequestMessage(null), "ABC");
 		assertTrue(payload.contains(baseConnector.getId().toString()));
	}
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHeaderStringFailed() {
		when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(null);
		assertNull(messageUtil.createResponsePayload(UtilMessageService.getDescriptionRequestMessage(null), "ABC"));
	}
	
	//Description request message in Http Headers
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHttpHeadersSuccessfull()  {
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
 		String payload = messageUtil.createResponsePayload(headers, null);
 		assertTrue(payload.contains(baseConnector.getId().toString()));
	}
	
	@Test
	public void testResponsePayloadWithoutRequestedElementInHttpHeadersFailed() {
		when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(null);
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
		assertNull(messageUtil.createResponsePayload(headers, null));
	}
	
	//Description request message with requested element
	//Description request message as Java Object
	
	@Test
	public void testResponsePayloadWithRequestedElementInHeaderMessageFailed() {
 		String payload = messageUtil.createResponsePayload(UtilMessageService.getDescriptionRequestMessage(NON_EXISTING_REQUESTED_ELEMENT_ID), null);
		assertTrue(payload.contains(RejectionMessage.class.getSimpleName()));
	}
	
	//Description request message as String
	
	@Test
	public void testResponsePayloadWithRequestedElementInHeaderStringSuccessfull()  {
		DescriptionRequestMessage drm = UtilMessageService.getDescriptionRequestMessage(EXISTING_REQUESTED_ELEMENT_ID);
 		String payload = messageUtil.createResponsePayload(drm, null);
 		assertTrue(payload.contains(EXISTING_REQUESTED_ELEMENT_ID.toString()));
	}
	
	@Test
	public void testResponsePayloadWithRequestedElementInHeaderStringFailed()  {
		DescriptionRequestMessage drm = UtilMessageService.getDescriptionRequestMessage(NON_EXISTING_REQUESTED_ELEMENT_ID);
 		String payload = messageUtil.createResponsePayload(drm, null);
 		assertTrue(payload.contains(RejectionMessage.class.getSimpleName()));
	}
	
	//Description request message in Http Headers
	
	@Test
	public void testResponsePayloadWithRequestedElementInHttpHeadersSuccessfull()  {
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
		headers.add(IDS_REQUESTED_ELEMENT, EXISTING_REQUESTED_ELEMENT_ID.toString());
 		String payload = messageUtil.createResponsePayload(headers, null);
 		assertTrue(payload.contains(EXISTING_REQUESTED_ELEMENT_ID.toString()));
	}
	
	@Test
	public void testResponsePayloadWithRequestedElementInHttpHeadersFailed() {
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
		headers.add(IDS_REQUESTED_ELEMENT, NON_EXISTING_REQUESTED_ELEMENT_ID.toString());
 		String payload = messageUtil.createResponsePayload(headers, null);
		assertEquals("IDS-RejectionReason:https://w3id.org/idsa/code/NOT_FOUND", payload);
	}	
	
	//Response for ContractRequestMessage
	
	@Test
	public void testResponsePayload_IDSContractRequestMessage()  {
		URI permissionId = baseConnector.getResourceCatalog().get(0).getOfferedResource().get(0).getContractOffer().get(0).getPermission().get(0).getId();
		String payload = messageUtil.createResponsePayload(UtilMessageService.getContractRequestMessage(), 
				UtilMessageService.getMessageAsString(UtilMessageService.getContractRequest(
						UtilMessageService.REQUESTED_ARTIFACT, permissionId)));
		assertTrue(payload.contains(ContractAgreement.class.getSimpleName()));
	}
	
	@Test
	public void testResponsePayload_StringContractRequestMessage()  {
		URI permissionId = baseConnector.getResourceCatalog().get(0).getOfferedResource().get(0).getContractOffer().get(0).getPermission().get(0).getId();
		String payload = messageUtil.createResponsePayload(UtilMessageService.getContractRequestMessage(),
				UtilMessageService.getMessageAsString(UtilMessageService.getContractRequest(UtilMessageService.REQUESTED_ARTIFACT, permissionId)));
		assertTrue(payload.contains(ContractAgreement.class.getSimpleName()));
	}
	
	//Response for ContractAgreementMessage
	
	@Test
	public void testResponsePayload_IDSContractAgreementMessage(){
		String payload = messageUtil.createResponsePayload(UtilMessageService.getContractAgreementMessage(), null);
		assertNull(payload);
	}

	@Test
	public void testResponsePayload_StringContractAgreementMessage(){
		String payload = messageUtil.createResponsePayload(UtilMessageService.getContractAgreementMessage(), null);
		assertNull(payload);;
	}
	
	//Response for ArtifactRequestMessage
	
	@Test
	public void testResponsePayload_IDSArtifactRequestMessage(){
		String payload = messageUtil.createResponsePayload(UtilMessageService.getArtifactRequestMessage(), "ABC");
		assertTrue(payload.contains("John"));
		assertTrue(payload.contains("Doe"));
		assertTrue(payload.contains("591  Franklin Street, Pennsylvania"));
	}

	@Test
	public void createArtifactResponseMessage() throws IOException  {
		// provide default ArtifactRequestMessage so it does not fail on check for
		// transfer contract and requested element
		Message message = messageUtil.createArtifactResponseMessage(UtilMessageService.getArtifactRequestMessage());
		assertNotNull(message);
		assertTrue(message instanceof ArtifactResponseMessage);
		serializer.serialize(message);
	}

	@Test
	public void createContractAgreementMessage() throws IOException  {
		Message message = messageUtil.createContractAgreementMessage(UtilMessageService.getContractRequestMessage());
		assertNotNull(message);
		assertTrue(message instanceof ContractAgreementMessage);
		serializer.serialize(message);
	}

	@Test
	public void createResultMessage() throws IOException  {
		Message message = messageUtil.createResultMessage(UtilMessageService.getArtifactRequestMessage());
		assertNotNull(message);
		assertTrue(message instanceof ResultMessage);
		serializer.serialize(message);
	}
	
	@Test
	public void bigPayload() throws UnsupportedOperationException, IOException {
		String header = UtilMessageService.getMessageAsString(UtilMessageService.getArtifactResponseMessage());

		HttpEntity httpEntity = messageUtil.createMultipartMessageForm(header, BigPayload.BIG_PAYLOAD, ContentType.TEXT_PLAIN);
		assertNotNull(httpEntity);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		httpEntity.writeTo(outStream);
		outStream.flush();

		String bigMultipartResponse = new String(outStream.toByteArray(), StandardCharsets.UTF_8);
		assertNotNull(bigMultipartResponse);
	}
}

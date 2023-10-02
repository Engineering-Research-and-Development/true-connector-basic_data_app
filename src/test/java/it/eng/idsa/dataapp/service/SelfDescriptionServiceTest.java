package it.eng.idsa.dataapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.service.impl.SelfDescriptionServiceImpl;
import it.eng.idsa.dataapp.web.rest.exceptions.InternalRecipientException;
import it.eng.idsa.dataapp.web.rest.exceptions.NotFoundException;
import it.eng.idsa.dataapp.web.rest.exceptions.TemporarilyNotAvailableException;
import it.eng.idsa.multipart.processor.util.SelfDescriptionUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

class SelfDescriptionServiceTest {

	private SelfDescriptionServiceImpl selfDescriptionService;

	@Mock
	private RestTemplate restTemplate;
	@Mock
	private RestTemplateBuilder restTemplateBuilder;
	@Mock
	private ECCProperties eccProperties;
	@Mock
	private ResponseEntity<String> response;

	@Mock
	private ThreadService threadService;
	private Connector baseConnector;

	private Serializer serializer = new Serializer();
	private Message message;
	private String selfDescriptionAsString;

	@BeforeEach
	public void init() throws IOException {
		MockitoAnnotations.openMocks(this);
		when(eccProperties.getHost()).thenReturn("fakeHost");
		when(eccProperties.getProtocol()).thenReturn("http");
		when(eccProperties.getPort()).thenReturn(1234);
		when(restTemplateBuilder.build()).thenReturn(restTemplate);
		selfDescriptionService = new SelfDescriptionServiceImpl(restTemplateBuilder, eccProperties);

		baseConnector = SelfDescriptionUtil.createDefaultSelfDescription();
		selfDescriptionAsString = serializer.serialize(baseConnector);
		when(response.getBody()).thenReturn(selfDescriptionAsString);
	}

	@Test
	void getSelfDescriptionTest() {
		
		when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(response);
		when(response.getStatusCodeValue()).thenReturn(200);
		Connector connector = selfDescriptionService.getSelfDescription(message);
		
		assertNotNull(connector);
	}

	@Test
	void getSelfDescriptionResponseStatusNotOkTest() {
		
		
		when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(response);
		message = UtilMessageService.getDescriptionRequestMessage(null);

		when(response.getStatusCodeValue()).thenReturn(400);
		InternalRecipientException exception = assertThrows(InternalRecipientException.class, () -> {
			selfDescriptionService.getSelfDescription(message);
		});

		assertEquals("Could not fetch self description", exception.getMessage());
	}

	@Test
	void getSelfDescriptionResponseNullTest() {
		when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(null);

		message = UtilMessageService.getDescriptionRequestMessage(null);
		
		TemporarilyNotAvailableException exception = assertThrows(TemporarilyNotAvailableException.class, () -> {
			selfDescriptionService.getSelfDescription(message);
		});

		assertEquals("Could not fetch self description, ECC did not respond", exception.getMessage());
	}

	@Test
	void getSelfDescriptionURISyntaxExceptionTest() {
		when(eccProperties.getProtocol()).thenReturn("\\");
		
		InternalRecipientException exception = assertThrows(InternalRecipientException.class, () -> {
			selfDescriptionService.getSelfDescription(message);
		});
		
		assertEquals("Could not create URI for Self Description request", exception.getMessage());
	}

	@Test
	void getSelfDescriptionAsStringTest() {
		when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(response);
		when(response.getStatusCodeValue()).thenReturn(200);
		String selfDescriptionAsString = selfDescriptionService.getSelfDescriptionAsString(message);
		
		assertNotNull(selfDescriptionAsString);
	}

	@Test
	void getSelfDescriptionAsStringTest2() {
		when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(response);
		when(response.getStatusCodeValue()).thenReturn(200);
		String selfDescriptionAsString = selfDescriptionService.getSelfDescriptionAsString(null);
		
		assertNotNull(selfDescriptionAsString);
	}

	@Test
	void getArtifactRequestedElement() {
		Message message = UtilMessageService.getArtifactRequestMessage();
		assertTrue(
				selfDescriptionService.artifactRequestedElementExist((ArtifactRequestMessage) message, baseConnector));
	}

	@Test
	void getArtifactRequestedElementNotExistTest() throws URISyntaxException {
		Message message = UtilMessageService.getArtifactRequestMessage(new URI("www.exceptipon.com"));
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			selfDescriptionService.artifactRequestedElementExist((ArtifactRequestMessage) message, baseConnector);
		});

		assertEquals("Requested element not found", exception.getMessage());
	}

	@Test
	void getRequestedElementTest() throws URISyntaxException {

		URI messageOfferedResource = new URI("");
		for (ResourceCatalog catalog : baseConnector.getResourceCatalog()) {
			for (Resource offeredResource : catalog.getOfferedResource()) {
				messageOfferedResource = offeredResource.getId();
			}
		}
		Message message = UtilMessageService.getDescriptionRequestMessage(messageOfferedResource);

		assertNotNull(selfDescriptionService.getRequestedElement((DescriptionRequestMessage) message, baseConnector));
	}

	@Test
	void getRequestedElementNotFoundExceptionTest() throws URISyntaxException {
		Message message = UtilMessageService.getDescriptionRequestMessage(new URI("www.google.com"));

		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			selfDescriptionService.getRequestedElement((DescriptionRequestMessage) message, baseConnector);
		});

		assertEquals("Requested element not found", exception.getMessage());
	}
}

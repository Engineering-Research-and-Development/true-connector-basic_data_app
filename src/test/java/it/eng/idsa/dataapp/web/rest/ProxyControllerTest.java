package it.eng.idsa.dataapp.web.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.eng.idsa.dataapp.domain.ProxyRequest;
import it.eng.idsa.dataapp.service.ProxyService;

public class ProxyControllerTest {

	@InjectMocks
	private ProxyController proxyController;
	@Mock
	private ProxyService proxyService;
	@Mock
	private HttpHeaders httpHeaders;
	private ProxyRequest proxyRequest;
	private HttpMethod method = HttpMethod.POST;
	String body = "{\"test\":\"test\"}";

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		proxyRequest = new ProxyRequest();
		proxyRequest.setMessageType("ArtifactReqeustMessage");
	}

	@Test
	public void proxyRequestWithMultipartFormTest() throws Exception {
		proxyRequest.setMultipart(ProxyRequest.MULTIPART_FORM);
		ResponseEntity<String> responseEntity = new ResponseEntity<>("response", HttpStatus.OK);

		when(proxyService.parseIncommingProxyRequest(body)).thenReturn(proxyRequest);
		when(proxyService.proxyMultipartForm(proxyRequest, httpHeaders)).thenReturn(responseEntity);

		ResponseEntity<?> response = proxyController.proxyRequest(httpHeaders, body, method);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("response", response.getBody());
	}

	@Test
	public void proxyRequestWithMultipartMixedTest() throws Exception {
		proxyRequest.setMultipart(ProxyRequest.MULTIPART_MIXED);
		ResponseEntity<String> responseEntity = new ResponseEntity<>("response", HttpStatus.OK);

		when(proxyService.parseIncommingProxyRequest(body)).thenReturn(proxyRequest);
		when(proxyService.proxyMultipartMix(proxyRequest, httpHeaders)).thenReturn(responseEntity);

		ResponseEntity<?> response = proxyController.proxyRequest(httpHeaders, body, method);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("response", response.getBody());
	}

	@Test
	public void proxyRequestWithHttpHeadersTest() throws Exception {
		proxyRequest.setMultipart(ProxyRequest.MULTIPART_HEADER);
		ResponseEntity<String> responseEntity = new ResponseEntity<>("response", HttpStatus.OK);

		when(proxyService.parseIncommingProxyRequest(body)).thenReturn(proxyRequest);
		when(proxyService.proxyHttpHeader(proxyRequest, httpHeaders)).thenReturn(responseEntity);

		ResponseEntity<?> response = proxyController.proxyRequest(httpHeaders, body, method);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("response", response.getBody());
	}

	@Test
	public void proxyRequestWithWssTest() throws Exception {

		proxyRequest.setMultipart(ProxyRequest.WSS);
		ResponseEntity<String> responseEntity = new ResponseEntity<>("response", HttpStatus.OK);

		when(proxyService.parseIncommingProxyRequest(body)).thenReturn(proxyRequest);
		when(proxyService.proxyWSSRequest(proxyRequest)).thenReturn(responseEntity);

		ResponseEntity<?> response = proxyController.proxyRequest(httpHeaders, body, method);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("response", response.getBody());
	}

	@Test
	public void proxyRequestWithWssRequestedArtifactTest() throws Exception {
		proxyRequest.setMultipart(ProxyRequest.WSS);
		proxyRequest.setRequestedArtifact("/test1");
		proxyRequest.setMessageType("ArtifactRequestMessage");
		ResponseEntity<String> responseEntity = new ResponseEntity<>("response", HttpStatus.OK);

		when(proxyService.parseIncommingProxyRequest(body)).thenReturn(proxyRequest);
		when(proxyService.proxyWSSRequest(proxyRequest)).thenReturn(responseEntity);

		ResponseEntity<?> response = proxyController.proxyRequest(httpHeaders, body, method);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("response", response.getBody());
	}

	@Test
	public void proxyRequestWithInvalidMultipartTest() throws Exception {

		proxyRequest.setMultipart("invalid");

		when(proxyService.parseIncommingProxyRequest(body)).thenReturn(proxyRequest);

		ResponseEntity<?> response = proxyController.proxyRequest(httpHeaders, body, method);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Missing proper value for MULTIPART, should be one of: 'mixed', 'form', 'http-header'",
				response.getBody());
	}

	@Test
	public void proxyRequestMissingMessageTypeTest() throws Exception {

		ProxyRequest proxyRequest = new ProxyRequest();
		proxyRequest.setMultipart(ProxyRequest.MULTIPART_MIXED);

		when(proxyService.parseIncommingProxyRequest(body)).thenReturn(proxyRequest);

		ResponseEntity<?> response = proxyController.proxyRequest(httpHeaders, body, method);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Message type part in body is mandatory for mixed flow", response.getBody());
	}

	@Test
	public void proxyRequestMissingMultipart() throws Exception {

		when(proxyService.parseIncommingProxyRequest(body)).thenReturn(proxyRequest);

		ResponseEntity<?> response = proxyController.proxyRequest(httpHeaders, body, method);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Multipart field not found in request, mandatory for the flow", response.getBody());
	}
}
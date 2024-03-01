package it.eng.idsa.dataapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.domain.ProxyRequest;
import it.eng.idsa.dataapp.ftp.client.FTPClient;
import it.eng.idsa.dataapp.repository.CheckSumRepository;
import it.eng.idsa.dataapp.service.impl.CheckSumServiceImpl;
import it.eng.idsa.dataapp.service.impl.ProxyServiceImpl;
import it.eng.idsa.dataapp.service.impl.SelfDescriptionValidator;
import it.eng.idsa.dataapp.util.RejectionUtil;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.UtilMessageService;

public class ProxyServiceTest {

	private static final String PAYLOAD = "Payload";
	private String messageType;

	private ProxyServiceImpl service;
	@Mock
	private RecreateFileService recreateFileService;
	@Mock
	private SelfDescriptionValidator selfDescriptionValidator;

	private HttpHeaders httpHeaders;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private RestTemplateBuilder restTemplateBuilder;
	@Mock
	private ECCProperties eccProperties;
	@Mock
	private ProxyRequest proxyRequest;
	@Mock
	private ResponseEntity<String> response;

	private String dataLakeDirectory;

	private String issuerConnector = "http://w3id.org/engrd/connector/";

	private Boolean encodePayload;

	private Boolean extractPayloadFromResponse;

	@Mock
	private CheckSumRepository checkSumRepository;

	@Mock
	private FTPClient ftpClient;

	private Path dataLakeDirectoryPath = Paths.get("src/test/resources");

	private Optional<CheckSumService> checkSumService = Optional
			.of(new CheckSumServiceImpl(checkSumRepository, dataLakeDirectoryPath));

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
		when(restTemplateBuilder.build()).thenReturn(restTemplate);
		encodePayload = false;
		extractPayloadFromResponse = false;
		service = new ProxyServiceImpl(restTemplateBuilder, eccProperties, recreateFileService, checkSumService,
				dataLakeDirectory, issuerConnector, encodePayload, extractPayloadFromResponse, selfDescriptionValidator,
				ftpClient);
		messageType = ArtifactRequestMessage.class.getSimpleName();
		when(eccProperties.getProtocol()).thenReturn("https");
		when(eccProperties.getHost()).thenReturn("test.host");
		when(eccProperties.getPort()).thenReturn(123);
		when(selfDescriptionValidator.validateSelfDescription(any(String.class))).thenReturn(true);
		httpHeaders = new HttpHeaders();
		httpHeaders.add("key", "value");
	}

	@Test
	public void proxyMultipartMix_Success() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_MIXED);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(response);
		String multipartMessageString = createMultipartMessageAsString(UtilMessageService.getArtifactRequestMessage());
		mockResponse(HttpStatus.OK, multipartMessageString);
		
		ResponseEntity<String> testResponse = service.proxyMultipartMix(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
		assertEquals(multipartMessageString, testResponse.getBody());
	}

	@Test
	public void proxyMultipartMix_Rejection() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_MIXED);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(response);
		String multipartMessageString = createMultipartMessageAsString(UtilMessageService.getRejectionMessage(RejectionReason.MALFORMED_MESSAGE));
		mockResponse(HttpStatus.OK, multipartMessageString);
		
		ResponseEntity<String> testResponse = service.proxyMultipartMix(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
		assertEquals(RejectionUtil.HANDLE_REJECTION(RejectionReason.MALFORMED_MESSAGE).getStatusCode(), testResponse.getStatusCode());
		assertEquals(RejectionUtil.HANDLE_REJECTION(RejectionReason.MALFORMED_MESSAGE).getBody(), testResponse.getBody());
	}

	@Test
	public void proxyMultipartMix_NoRejectionReason() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_MIXED);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(response);
		String multipartMessageString = createMultipartMessageAsString(UtilMessageService.getRejectionMessage(null));
		mockResponse(HttpStatus.OK, multipartMessageString);
		
		ResponseEntity<String> testResponse = service.proxyMultipartMix(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
		assertEquals(HttpStatus.BAD_REQUEST, testResponse.getStatusCode());
		assertEquals("Error while processing message", testResponse.getBody());
	}

	@Test
	public void proxyMultipartMix_extractPayloadFromResponse() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_MIXED);
		ReflectionTestUtils.setField(service, "extractPayloadFromResponse", true);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(response);
		String multipartMessageString = createMultipartMessageAsString(UtilMessageService.getArtifactRequestMessage());
		mockResponse(HttpStatus.OK, multipartMessageString);
		
		ResponseEntity<String> testResponse = service.proxyMultipartMix(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
		assertEquals(PAYLOAD, testResponse.getBody());
	}

	@Test
	public void proxyMultipartForm_Success() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_FORM);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(response);
		String multipartMessageString = createMultipartMessageAsString(UtilMessageService.getArtifactRequestMessage());
		mockResponse(HttpStatus.OK, multipartMessageString);
		
		ResponseEntity<String> testResponse = service.proxyMultipartForm(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
		assertEquals(multipartMessageString, testResponse.getBody());
	}

	@Test
	public void proxyMultipartForm_Rejection() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_FORM);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(response);
		String multipartMessageString = createMultipartMessageAsString(UtilMessageService.getRejectionMessage(RejectionReason.MALFORMED_MESSAGE));
		mockResponse(HttpStatus.OK, multipartMessageString);
		
		ResponseEntity<String> testResponse = service.proxyMultipartForm(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
		assertEquals(RejectionUtil.HANDLE_REJECTION(RejectionReason.MALFORMED_MESSAGE).getStatusCode(), testResponse.getStatusCode());
		assertEquals(RejectionUtil.HANDLE_REJECTION(RejectionReason.MALFORMED_MESSAGE).getBody(), testResponse.getBody());
	}

	@Test
	public void proxyMultipartForm_NoRejectionReason() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_FORM);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(response);
		String multipartMessageString = createMultipartMessageAsString(UtilMessageService.getRejectionMessage(null));
		mockResponse(HttpStatus.OK, multipartMessageString);
		
		ResponseEntity<String> testResponse = service.proxyMultipartForm(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
		assertEquals(HttpStatus.BAD_REQUEST, testResponse.getStatusCode());
		assertEquals("Error while processing message", testResponse.getBody());
	}

	@Test
	public void proxyMultipartForm_extractPayloadFromResponse() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_FORM);
		ReflectionTestUtils.setField(service, "extractPayloadFromResponse", true);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(response);
		String multipartMessageString = createMultipartMessageAsString(UtilMessageService.getArtifactRequestMessage());
		mockResponse(HttpStatus.OK, multipartMessageString);
		
		ResponseEntity<String> testResponse = service.proxyMultipartForm(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
		assertEquals(PAYLOAD, testResponse.getBody());
	}

	private String createMultipartMessageAsString(Message message) {

		MultipartMessage mm = new MultipartMessageBuilder().withHeaderContent(message).withPayloadContent(PAYLOAD)
				.build();
		return MultipartMessageProcessor.multipartMessagetoString(mm);
	}

	@Test
	public void proxyMultipartHeader_Success() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_HEADER);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		
		Map<String, Object> message = UtilMessageService.getArtifactResponseMessageAsMap();
		
		HttpHeaders messageHeaders = new HttpHeaders();
		
		for (String key : message.keySet()) {
			messageHeaders.add(key, message.get(key).toString());
		}
		
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				any(HttpEntity.class), eq(String.class)))
			.thenReturn(response);
		mockResponseForHttpHeaders(HttpStatus.OK, PAYLOAD, messageHeaders);
		
		ResponseEntity<String> testResponse = service.proxyHttpHeader(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
		assertEquals(HttpStatus.OK, testResponse.getStatusCode());
		assertEquals(messageHeaders.get("IDS-Messagetype"), testResponse.getHeaders().get("IDS-Messagetype"));
		assertEquals(PAYLOAD, testResponse.getBody());
	}

	@Test
	public void proxyMultipartHeader_Rejection() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_HEADER);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		
		Map<String, Object> message = UtilMessageService.getArtifactResponseMessageAsMap();
		
		// changing values to rejection message
		message.replace("IDS-Messagetype", message.get("IDS-Messagetype"), "ids:RejectionMessage");
		message.put("IDS-RejectionReason", RejectionReason.MALFORMED_MESSAGE);
		
		HttpHeaders messageHeaders = new HttpHeaders();
		
		for (String key : message.keySet()) {
			messageHeaders.add(key, message.get(key).toString());
		}
		
		mockResponseForHttpHeaders(HttpStatus.OK, PAYLOAD, messageHeaders);
		
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				any(HttpEntity.class), eq(String.class)))
			.thenReturn(response);
		
		ResponseEntity<String> testResponse = service.proxyHttpHeader(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
		assertEquals(RejectionUtil.HANDLE_REJECTION(RejectionReason.MALFORMED_MESSAGE).getStatusCode(), testResponse.getStatusCode());
		assertEquals(RejectionUtil.HANDLE_REJECTION(RejectionReason.MALFORMED_MESSAGE).getBody(), testResponse.getBody());
	}

	@Test
	public void proxyMultipartHeader_NoRejectionReason() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_HEADER);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		
		Map<String, Object> message = UtilMessageService.getArtifactResponseMessageAsMap();
		
		// changing values to rejection message
		message.replace("IDS-Messagetype", message.get("IDS-Messagetype"), "ids:RejectionMessage");
		message.put("IDS-RejectionReason", null);
		
		HttpHeaders messageHeaders = new HttpHeaders();
		
		for (String key : message.keySet()) {
			if ( message.get(key) != null) {
				messageHeaders.add(key, message.get(key).toString());
			} else {
				messageHeaders.add(key, null);
			}
		}
		
		mockResponseForHttpHeaders(HttpStatus.OK, PAYLOAD, messageHeaders);
		
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				any(HttpEntity.class), eq(String.class)))
			.thenReturn(response);
		
		ResponseEntity<String> testResponse = service.proxyHttpHeader(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
		assertEquals(HttpStatus.BAD_REQUEST, testResponse.getStatusCode());
		assertEquals("Error while processing message", testResponse.getBody());
	}

	@Test
	public void proxyMultipartHeader_extractPayloadFromResponse() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_HEADER);
		ReflectionTestUtils.setField(service, "extractPayloadFromResponse", true);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		
		Map<String, Object> message = UtilMessageService.getArtifactResponseMessageAsMap();
		
		HttpHeaders messageHeaders = new HttpHeaders();
		
		for (String key : message.keySet()) {
			messageHeaders.add(key, message.get(key).toString());
		}
		
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				any(HttpEntity.class), eq(String.class)))
			.thenReturn(response);
		mockResponseForHttpHeaders(HttpStatus.OK, PAYLOAD, messageHeaders);
		
		ResponseEntity<String> testResponse = service.proxyHttpHeader(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
		assertEquals(HttpStatus.OK, testResponse.getStatusCode());
		assertNull(testResponse.getHeaders().get("IDS-Messagetype"));
		assertEquals(PAYLOAD, testResponse.getBody());
	}

	private void mockResponse(HttpStatus status, String body) {
		when(response.getStatusCode()).thenReturn(status);
		when(response.getHeaders()).thenReturn(httpHeaders);
		when(response.getBody()).thenReturn(body);
	}

	private void mockResponseForHttpHeaders(HttpStatus status, String body, HttpHeaders headers) {
		when(response.getStatusCode()).thenReturn(status);
		when(response.getHeaders()).thenReturn(headers);
		when(response.getBody()).thenReturn(body);
	}

	@Test
	public void parseIncommingProxyRequest() {
		ProxyRequest pr = service.parseIncommingProxyRequest(getProxyRequest());
		assertNotNull(pr);
		assertEquals(ProxyRequest.MULTIPART_MIXED, pr.getMultipart());
		assertNotNull(pr.getPayload());
		assertNotNull(pr.getMessageType());
	}

	@Test
	public void parseIncommingProxyRequestWssRequestArtifact() {
		ProxyRequest pr = service.parseIncommingProxyRequest(getProxyRequestRequestedArtifact());
		assertNotNull(pr);
		assertEquals(ProxyRequest.WSS, pr.getMultipart());
		assertEquals("test.csv", pr.getRequestedArtifact());
		assertNull(pr.getPayload());
		assertNull(pr.getMessageType());
	}

	@Test
	public void parseIncommingProxyRequestWssContractAgreement() {
		ProxyRequest pr = service.parseIncommingProxyRequest(getProxyRequestContractRequest());
		assertNotNull(pr);
		assertEquals(ProxyRequest.WSS, pr.getMultipart());
		assertNull(pr.getRequestedArtifact());
		assertNotNull(pr.getPayload());
		assertEquals(ContractAgreementMessage.class.getSimpleName(), pr.getMessageType());
	}

	@Test
	public void parseJsonPayload() {
		ProxyRequest pr = service.parseIncommingProxyRequest(getJsonPayload());
		assertNotNull(pr);
		assertEquals(ProxyRequest.MULTIPART_HEADER, pr.getMultipart());
		assertNotNull(pr.getPayload());
		assertEquals(ArtifactRequestMessage.class.getSimpleName(), pr.getMessageType());
	}

	@Test
	public void parseStringPayload() {
		ProxyRequest pr = service.parseIncommingProxyRequest(getStringPayload());
		assertNotNull(pr);
		assertEquals(ProxyRequest.MULTIPART_HEADER, pr.getMultipart());
		assertNotNull(pr.getPayload());
		assertEquals("SELECT ?connectorUri WHERE { ?connectorUri a ids:BaseConnector . }", pr.getPayload());
		assertEquals(ArtifactRequestMessage.class.getSimpleName(), pr.getMessageType());
	}

	private String getProxyRequest() {
		return "{\r\n" + "   \"multipart\": \"mixed\",\r\n" + "   \"messageType\": \"ArtifactRequestMessage\",\r\n"
				+ "	\"payload\" : {\r\n" + "		\"catalog.offers.0.resourceEndpoints.path\":\"/pet2\"\r\n"
				+ "		},\r\n" + "}";
	}

	private String getProxyRequestRequestedArtifact() {
		return "{\r\n" + "    \"multipart\": \"wss\",\r\n" + "    \"requestedArtifact\": \"test.csv\",\r\n" + "}";
	}

	private String getProxyRequestContractRequest() {
		return "{\r\n" + "    \"multipart\": \"wss\",\r\n" + "    \"Forward-To\": \"wss://localhost:8086\",\r\n"
				+ "    \"Forward-To-Internal\": \"wss://localhost:8887\",\r\n"
				+ "    \"messageType\": \"ContractAgreementMessage\",\r\n" + "\r\n" + "    \"payload\": "
				+ UtilMessageService.getMessageAsString(UtilMessageService.getContractAgreement()) + "\r\n" + "}\r\n";
	}

	private String getJsonPayload() {
		return "{\r\n" + "    \"multipart\": \"http-header\",\r\n"
				+ "    \"Forward-To\": \"https://ecc-provider:8086/data\",\r\n"
				+ "    \"messageType\":\"ArtifactRequestMessage\",\r\n" + "	 \"payload\" : {\r\n"
				+ "		\"catalog.offers.0.resourceEndpoints.path\":\"/pet2\"\r\n" + "		}\r\n" + "}";
	}

	private String getStringPayload() {
		return "{\r\n" + "    \"multipart\": \"http-header\",\r\n"
				+ "    \"Forward-To\": \"https://ecc-provider:8086/data\",\r\n"
				+ "    \"messageType\":\"ArtifactRequestMessage\",\r\n"
				+ "	 \"payload\" : \"SELECT ?connectorUri WHERE \\{ ?connectorUri a ids:BaseConnector . \\}\"" + "}";
	}
}

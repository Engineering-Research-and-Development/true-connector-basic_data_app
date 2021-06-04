package it.eng.idsa.dataapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

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
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryMessage;
import de.fraunhofer.iais.eis.QueryMessageBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.domain.ProxyRequest;
import it.eng.idsa.dataapp.service.impl.MultiPartMessageServiceImpl;
import it.eng.idsa.dataapp.service.impl.ProxyServiceImpl;
import it.eng.idsa.multipart.util.DateUtil;

public class ProxyServiceTest {

	private static final String PAYLOAD = "Payload";
	private static final String QUERY_STRING = "PREFIX ids: <https://w3id.org/idsa/core/>\r\n" + 
				"SELECT ?connectorUri WHERE { ?connectorUri a ids:BaseConnector . }";
	private String message;
	
	public static URI ISSUER_CONNECTOR = URI.create("http://w3id.org/engrd/connector");
	public static URI RECIPIENT_CONNECTOR = URI.create("http://w3id.org/engrd/connector/recipient");
	public static URI SENDER_AGENT = URI.create("http://sender.agent/sender");
	public static URI AFFECTED_CONNECOTR = URI.create("https://affected.connecotr");

	private ProxyServiceImpl service;
	@Mock
	private MultiPartMessageServiceImpl multiPartMessageService;
	@Mock
	private RecreateFileService recreateFileService;
	@Mock
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
	private Map<String, Object> messageAsHeaders;
	private String dataLakeDirectory ;
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(restTemplateBuilder.build()).thenReturn(restTemplate);
		service = new ProxyServiceImpl(restTemplateBuilder, eccProperties, multiPartMessageService, recreateFileService, dataLakeDirectory);
		message = getMessageJson();
		when(eccProperties.getProtocol()).thenReturn("https");
		when(eccProperties.getHost()).thenReturn("test.host");
		when(eccProperties.getPort()).thenReturn(123);
		mockMessageAsHeaders();
	}

	@Test
	public void proxyMultipartMix_Success() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_MIXED);

		when(proxyRequest.getMessage()).thenReturn(message);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(response);
		mockResponse(HttpStatus.OK);
		
		service.proxyMultipartMix(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
	}
	
	@Test
	public void proxyMultipartForm_Success() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_FORM);

		when(proxyRequest.getMessage()).thenReturn(message);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(response);
		mockResponse(HttpStatus.OK);
		
		service.proxyMultipartForm(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
	}
	
	@Test
	public void proxyMultipartHeader_Success() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_HEADER);

		when(proxyRequest.getMessageAsHeader()).thenReturn(messageAsHeaders);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(response);
		mockResponse(HttpStatus.OK);
		
		service.proxyHttpHeader(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
	}
	
	private void mockResponse(HttpStatus status) {
		when(response.getStatusCode()).thenReturn(status);
		when(response.getHeaders()).thenReturn(httpHeaders);
		when(response.getBody()).thenReturn("Response Body");
	}
	
	private void mockMessageAsHeaders() {
		messageAsHeaders = new HashMap<>();
		messageAsHeaders.put("IDS-Messagetype", "ids:ArtifactRequestMessage");
		messageAsHeaders.put("IDS-Id", "https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");
		messageAsHeaders.put("IDS-Issued", "2021-01-15T13:09:42.306Z");
		messageAsHeaders.put("IDS-IssuerConnector", "http://w3id.org/engrd/connector/");
		messageAsHeaders.put("IDS-ModelVersion", "4.0.0");
		messageAsHeaders.put("IDS-RequestedArtifact", "http://w3id.org/engrd/connector/artifact/1");
	}
	
	@Test
	public void parseIncommingProxyRequest() {
		ProxyRequest pr = service.parseIncommingProxyRequest(getProxyRequest());
		assertNotNull(pr);
		assertEquals(ProxyRequest.MULTIPART_MIXED, pr.getMultipart());
		assertNotNull(pr.getPayload());
		assertNotNull(pr.getMessage());
		assertFalse(CollectionUtils.isEmpty(pr.getMessageAsHeader()));
	}
	
	@Test
	public void parseIncommingProxyRequestWssRequestArtifact() {
		ProxyRequest pr = service.parseIncommingProxyRequest(getProxyRequestRequestedArtifact());
		assertNotNull(pr);
		assertEquals(ProxyRequest.WSS, pr.getMultipart());
		assertEquals("test.csv", pr.getRequestedArtifact());
		assertNull(pr.getPayload());
		assertNull(pr.getMessage());
		assertTrue(CollectionUtils.isEmpty(pr.getMessageAsHeader()));
	}
	
	@Test
	public void parseIncommingProxyRequestWssContractAgreement() {
		ProxyRequest pr = service.parseIncommingProxyRequest(getProxyRequestContractRequest());
		assertNotNull(pr);
		assertEquals(ProxyRequest.WSS, pr.getMultipart());
		assertNull(pr.getRequestedArtifact());
		assertNotNull(pr.getPayload());
		assertNotNull(pr.getMessage());
		assertTrue(CollectionUtils.isEmpty(pr.getMessageAsHeader()));
	}
	
	@Test
	public void parseStringPayload() {
		ProxyRequest pr = service.parseIncommingProxyRequest(getStringPayload());
		assertNotNull(pr);
		assertEquals(ProxyRequest.MULTIPART_HEADER, pr.getMultipart());
		assertNotNull(pr.getPayload());
		assertEquals(QUERY_STRING, pr.getPayload());
	}

	private String getProxyRequest() {
		return "{\r\n" + 
				"    \"multipart\": \"mixed\",\r\n" + 
				"	\"message\": {\r\n" + 
				"	  \"@context\" : {\r\n" + 
				"		\"ids\" : \"https://w3id.org/idsa/core/\"\r\n" + 
				"	  },\r\n" + 
				"	  \"@type\" : \"ids:ArtifactRequestMessage\",\r\n" + 
				"	  \"@id\" : \"https://w3id.org/idsa/autogen/artifactRequestMessage/76481a41-8117-4c79-bdf4-9903ef8f825a\",\r\n" + 
				"	  \"ids:issued\" : {\r\n" + 
				"		\"@value\" : \"2020-11-25T16:43:27.051+01:00\",\r\n" + 
				"		\"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"	  },\r\n" + 
				"	  \"ids:modelVersion\" : \"4.0.0\",\r\n" + 
				"	  \"ids:issuerConnector\" : {\r\n" + 
				"		\"@id\" : \"http://w3id.org/engrd/connector/\"\r\n" + 
				"	  },\r\n" + 
				"	  \"ids:requestedArtifact\" : {\r\n" + 
				"	   \"@id\" : \"http://w3id.org/engrd/connector/artifact/1\"\r\n" + 
				"	  }\r\n" + 
				"	},\r\n" + 
				"	\"payload\" : {\r\n" + 
				"		\"catalog.offers.0.resourceEndpoints.path\":\"/pet2\"\r\n" + 
				"		},\r\n" + 
				"    \"messageAsHeaders\": {\r\n" + 
				"        \"IDS-RequestedArtifact\":\"http://w3id.org/engrd/connector/artifact/1\",\r\n" + 
				"        \"IDS-Messagetype\":\"ids:ArtifactRequestMessage\",\r\n" + 
				"        \"IDS-ModelVersion\":\"4.0.0\",\r\n" + 
				"        \"IDS-Issued\":\"2021-01-15T13:09:42.306Z\",\r\n" + 
				"        \"IDS-Id\":\"https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f\",\r\n" + 
				"        \"IDS-IssuerConnector\":\"http://w3id.org/engrd/connector/\"\r\n" + 
				"        }\r\n" + 
				"}";
	}
	
	private String getProxyRequestRequestedArtifact() {
		return "{\r\n" + 
				"    \"multipart\": \"wss\",\r\n" + 
				"    \"requestedArtifact\": \"test.csv\",\r\n" + 
				"}";
	}
	
	private String getStringPayload() {
		return "{\r\n" + 
				"    \"multipart\": \"http-header\",\r\n" + 
				"    \"requestedArtifact\": \"test.csv\",\r\n" + 
				"    \"payload\": \"PREFIX ids: <https://w3id.org/idsa/core/>\r\nSELECT ?connectorUri WHERE { ?connectorUri a ids:BaseConnector . }\", "+
				"}";
	}
	
	private String getProxyRequestContractRequest() {
		return "{\r\n" + 
				"    \"multipart\": \"wss\",\r\n" + 
				"    \"Forward-To\": \"wss://localhost:8086\",\r\n" + 
				"    \"Forward-To-Internal\": \"wss://localhost:8887\",\r\n" + 
				"    \"message\": {\r\n" + 
				"\r\n" + 
				"        \"@type\": \"ids:ContractRequestMessage\",\r\n" + 
				"        \"issued\": \"2019-05-28T16:06:56.201Z\",\r\n" + 
				"        \"issuerConnector\": \"http://w3id.org/engrd/connector\",\r\n" + 
				"        \"correlationMessage\": \"https://w3id.org/idsa/autogen/contractOfferMessage/9bfedd37-d592-4064-b440-b9b82b9cc6fb\",\r\n" + 
				"        \"transferContract\": \"http://w3id.org/engrd/1559059616204\",\r\n" + 
				"        \"modelVersion\": \"1.0.2-SNAPSHOT\",\r\n" + 
				"        \"@id\": \"https://w3id.org/idsa/autogen/contractAgreementMessage/dd65f60d-994d-4f0a-9050-e46a13300e8e\"\r\n" + 
				"    },\r\n" + 
				"    \"payload\": {\r\n" + 
				"        \"@context\": \"https://w3id.org/idsa/contexts/context.jsonld\",\r\n" + 
				"        \"@type\": \"ids:ContractRequest\",\r\n" + 
				"        \"@id\": \"https://w3id.org/engrd/connector/examplecontract/bab-bayernsample/\",\r\n" + 
				"        \"consumer\": \"https://w3id.org/engrd/connector/consumer\",\r\n" + 
				"        \"provider\": \"https://w3id.org/engrd/connector/provider\",\r\n" + 
				"        \"permissions\": [{\r\n" + 
				"                \"@type\": \"ids:Permission\",\r\n" + 
				"                \"actions\": [{\r\n" + 
				"                        \"@id\": \"https://w3id.org/idsa/code/action/USE\"\r\n" + 
				"                    }\r\n" + 
				"                ],\r\n" + 
				"                \"constraints\": [{\r\n" + 
				"                        \"@type\": \"ids:Constraint\",\r\n" + 
				"                        \"operator\": {\r\n" + 
				"                            \"@id\": \"https://w3id.org/idsa/core/gt\"\r\n" + 
				"                        },\r\n" + 
				"                        \"leftOperand\": {\r\n" + 
				"                            \"@id\": \"https://w3id.org/idsa/core/DATE_TIME\"\r\n" + 
				"                        },\r\n" + 
				"                        \"rightOperand\": {\r\n" + 
				"                            \"@value\": \"\\\"2019-01-01T00:00:00.000+00:00\\\"^^xsd:dateTime\"\r\n" + 
				"                        }\r\n" + 
				"                    }, {\r\n" + 
				"                        \"@type\": \"ids:Constraint\",\r\n" + 
				"                        \"operator\": {\r\n" + 
				"                            \"@id\": \"https://w3id.org/idsa/core/lt\"\r\n" + 
				"                        },\r\n" + 
				"                        \"leftOperand\": {\r\n" + 
				"                            \"@id\": \"https://w3id.org/idsa/core/DATE_TIME\"\r\n" + 
				"                        },\r\n" + 
				"                        \"rightOperand\": {\r\n" + 
				"                            \"@value\": \"\\\"2019-12-31T23:59:59.999+00:00\\\"^^xsd:dateTime\"\r\n" + 
				"                        }\r\n" + 
				"                    }\r\n" + 
				"                ]\r\n" + 
				"            }\r\n" + 
				"        ],\r\n" + 
				"        \"contractDocument\": {\r\n" + 
				"            \"@type\": \"ids:TextResource\",\r\n" + 
				"            \"@id\": \"https://creativecommons.org/licenses/by-nc/4.0/legalcode\"\r\n" + 
				"        }\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"}\r\n";
	}

	// TODO replace with java implementation or TestUtilMessageService
	private String getMessageJson() {
		return "{\r\n" + 
				"	  \"@context\" : {\r\n" + 
				"		\"ids\" : \"https://w3id.org/idsa/core/\"\r\n" + 
				"	  },\r\n" + 
				"	  \"@type\" : \"ids:ArtifactRequestMessage\",\r\n" + 
				"	  \"@id\" : \"https://w3id.org/idsa/autogen/artifactRequestMessage/76481a41-8117-4c79-bdf4-9903ef8f825a\",\r\n" + 
				"	  \"ids:issued\" : {\r\n" + 
				"		\"@value\" : \"2020-11-25T16:43:27.051+01:00\",\r\n" + 
				"		\"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"	  },\r\n" + 
				"	  \"ids:modelVersion\" : \"4.0.0\",\r\n" + 
				"	  \"ids:issuerConnector\" : {\r\n" + 
				"		\"@id\" : \"http://w3id.org/engrd/connector/\"\r\n" + 
				"	  },\r\n" + 
				"	  \"ids:requestedArtifact\" : {\r\n" + 
				"	   \"@id\" : \"http://w3id.org/engrd/connector/artifact/1\"\r\n" + 
				"	  }\r\n" + 
				"	}";
	}
	
	@Test
	public void getPaylaodRequestAsJson() throws IOException {
		ProxyRequest pr = new ProxyRequest();
		pr.setForwardTo("https://broker.ids.isst.fraunhofer.de/infrastructure");
		pr.setMultipart("mixed");
		Serializer s = new Serializer();
		String message = s.serialize(getQueryMessage(SENDER_AGENT, ISSUER_CONNECTOR, QueryLanguage.SPARQL));
		pr.setMessage(message);
		pr.setPayload(QUERY_STRING);
		
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(mapper.writeValueAsString(pr));
	}
	
	
	private QueryMessage getQueryMessage(URI senderAgent, URI issuerConnector, QueryLanguage queryLanguage) {
		return new QueryMessageBuilder() 
				._modelVersion_("4.0.0")
				._issued_(DateUtil.now())
				._senderAgent_(senderAgent)
				._issuerConnector_(issuerConnector)
				._queryLanguage_(queryLanguage)
//				._securityToken_(getDynamicAttributeToken())
				.build();
	}
}

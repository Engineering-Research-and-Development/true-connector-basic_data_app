package it.eng.idsa.dataapp.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.multipart.processor.util.SelfDescriptionUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

class DescriptionRequestMessageHandlerTest {

	@InjectMocks
	private DescriptionRequestMessageHandler descriptionRequestMessageHandler;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private RestTemplateBuilder restTemplateBuilder;
	@Mock
	private ECCProperties eccProperties;
	@Mock
	private ResponseEntity<String> response;
	private Message message;
	Map<String, Object> responseMap = new HashMap<>();
	private Serializer serializer = new Serializer();
	private Connector baseConnector;
	private String issuerConnector = "http://w3id.org/engrd/connector/";

	@BeforeEach
	public void init() throws IOException, URISyntaxException {

		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(descriptionRequestMessageHandler, "restTemplate", restTemplate);
		ReflectionTestUtils.setField(descriptionRequestMessageHandler, "issuerConnector", issuerConnector);
		message = UtilMessageService.getDescriptionRequestMessage(null);
		baseConnector = SelfDescriptionUtil.createDefaultSelfDescription();
		String selfDescriptionAsString = serializer.serialize(baseConnector);
		when(eccProperties.getHost()).thenReturn("fakeHost");
		when(eccProperties.getProtocol()).thenReturn("http");
		when(eccProperties.getPort()).thenReturn(1234);
		when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(response);
		when(response.getBody()).thenReturn(selfDescriptionAsString);
		when(response.getStatusCodeValue()).thenReturn(200);
	}

	@Test
	void handleMessageTest() {

		responseMap = descriptionRequestMessageHandler.handleMessage(message, "asdsad");

		assertNotNull(responseMap.get("header"));
		assertNotNull(responseMap.get("payload"));
		assertTrue(StringUtils.containsIgnoreCase(responseMap.get("header").toString(), message.getId().toString()));
	}
}

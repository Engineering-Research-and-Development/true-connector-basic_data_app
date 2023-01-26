package it.eng.idsa.dataapp.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;

import de.fraunhofer.iais.eis.Message;

import it.eng.idsa.multipart.util.UtilMessageService;

class HttpHeadersUtilTest {

	private HttpHeadersUtil httpHeadersUtil;
	@Mock
	private HttpHeaders htppHeaders;

	@BeforeEach
	public void init() {

		httpHeadersUtil = new HttpHeadersUtil();

		htppHeaders = new HttpHeaders();
		htppHeaders.add("ids-messagetype", "ids:ArtifactResponseMessage");
		htppHeaders.add("ids-issued", UtilMessageService.ISSUED.toString());
		htppHeaders.add("ids-issuerconnector", UtilMessageService.ISSUER_CONNECTOR.toString());
		htppHeaders.add("ids-correlationmessage", UtilMessageService.CORRELATION_MESSAGE.toString());
		htppHeaders.add("ids-transfercontract", UtilMessageService.TRANSFER_CONTRACT.toString());
		htppHeaders.add("ids-modelversion", UtilMessageService.MODEL_VERSION);
		htppHeaders.add("foo", "bar");
		htppHeaders.add("Forward-To", "https://forwardToURL");
	}

	@Test
	public void messageToHeadersTest_DescriptionRequestMessage() {
		Message message = UtilMessageService.getDescriptionRequestMessage(null);

		Map<String, Object> headers = httpHeadersUtil.messageToHeaders(message);

		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype"), "ids:DescriptionRequestMessage");
		assertEquals(headers.get("IDS-Id"), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue"), message.getSecurityToken().getTokenValue());
	}

	@Test
	public void messageToHeadersTest_ContractRequestMessage() {
		Message message = UtilMessageService.getContractRequestMessage();

		Map<String, Object> headers = httpHeadersUtil.messageToHeaders(message);

		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype"), "ids:ContractRequestMessage");
		assertEquals(headers.get("IDS-Id"), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue"), message.getSecurityToken().getTokenValue());
	}

	@Test
	public void messageToHeadersTest_ContractAgreementMessage() {
		Message message = UtilMessageService.getContractAgreementMessage();

		Map<String, Object> headers = httpHeadersUtil.messageToHeaders(message);

		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype"), "ids:ContractAgreementMessage");
		assertEquals(headers.get("IDS-Id"), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue"), message.getSecurityToken().getTokenValue());
	}

	@Test
	public void messageToHeadersTest_ArtifactRequestMessage() {
		Message message = UtilMessageService.getArtifactRequestMessage();

		Map<String, Object> headers = httpHeadersUtil.messageToHeaders(message);

		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype"), "ids:ArtifactRequestMessage");
		assertEquals(headers.get("IDS-Id"), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue"), message.getSecurityToken().getTokenValue());
	}

	@Test
	public void headersToMessage_DescriptionRequestMessage() {
		htppHeaders.add("ids-id",
				"https://w3id.org/idsa/autogen/descriptionRequestMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");

		Map<String, Object> httpHeadersToMap = httpHeadersUtil.httpHeadersToMap(htppHeaders);
		Message httpHeadersToMessage = httpHeadersUtil.headersToMessage(httpHeadersToMap);

		assertNotNull(httpHeadersToMessage);
		assertEquals(htppHeaders.get("ids-id").get(0), httpHeadersToMessage.getId().toString());
	}

	@Test
	public void headersToMessage_ContractRequestMessage() {
		htppHeaders.add("ids-id",
				"https://w3id.org/idsa/autogen/contractRequestMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");

		Map<String, Object> httpHeadersToMap = httpHeadersUtil.httpHeadersToMap(htppHeaders);
		Message httpHeadersToMessage = httpHeadersUtil.headersToMessage(httpHeadersToMap);

		assertNotNull(httpHeadersToMessage);
		assertEquals(htppHeaders.get("ids-id").get(0), httpHeadersToMessage.getId().toString());
	}

	@Test
	public void headersToMessage_ContractAgreementMessage() {
		htppHeaders.add("ids-id",
				"https://w3id.org/idsa/autogen/contractAgreementMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");

		Map<String, Object> httpHeadersToMap = httpHeadersUtil.httpHeadersToMap(htppHeaders);
		Message httpHeadersToMessage = httpHeadersUtil.headersToMessage(httpHeadersToMap);

		assertNotNull(httpHeadersToMessage);
		assertEquals(htppHeaders.get("ids-id").get(0), httpHeadersToMessage.getId().toString());
	}

	@Test
	public void headersToMessage_ArtifactRequestMessage() {
		htppHeaders.add("ids-id",
				"https://w3id.org/idsa/autogen/artifactRequestMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");

		Map<String, Object> httpHeadersToMap = httpHeadersUtil.httpHeadersToMap(htppHeaders);
		Message httpHeadersToMessage = httpHeadersUtil.headersToMessage(httpHeadersToMap);

		assertNotNull(httpHeadersToMessage);
		assertEquals(htppHeaders.get("ids-id").get(0), httpHeadersToMessage.getId().toString());
	}

	@Test
	public void httpHeadersToMapTest() {
		htppHeaders.add("ids-id",
				"https://w3id.org/idsa/autogen/descriptionRequestMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");

		Map<String, Object> httpHeadersToMap = httpHeadersUtil.httpHeadersToMap(htppHeaders);

		assertNotNull(httpHeadersToMap);
		assertEquals(htppHeaders.get("ids-id").get(0), httpHeadersToMap.get("ids-id"));
	}

	@Test
	public void createResponseMessageHeadersTest() {
		htppHeaders.add("ids-id",
				"https://w3id.org/idsa/autogen/descriptionRequestMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");

		Map<String, Object> httpHeadersToMap = httpHeadersUtil.httpHeadersToMap(htppHeaders);

		HttpHeaders messageToHeaders = httpHeadersUtil.createResponseMessageHeaders(httpHeadersToMap);

		assertNotNull(messageToHeaders);
		assertEquals(htppHeaders.get("ids-messagetype"), messageToHeaders.get("ids-messagetype"));
		assertEquals(htppHeaders.get("ids-id"), messageToHeaders.get("ids-id"));
	}

}

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

	@Mock
	private HttpHeaders htppHeaders;

	@BeforeEach
	public void init() {

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

		Map<String, Object> headers = HttpHeadersUtil.messageToHttpHeaders(message);

		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype"), "ids:DescriptionRequestMessage");
		assertEquals(headers.get("IDS-Id"), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue"), message.getSecurityToken().getTokenValue());
	}

	@Test
	public void messageToHeadersTest_ContractRequestMessage() {
		Message message = UtilMessageService.getContractRequestMessage();

		Map<String, Object> headers = HttpHeadersUtil.messageToHttpHeaders(message);

		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype"), "ids:ContractRequestMessage");
		assertEquals(headers.get("IDS-Id"), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue"), message.getSecurityToken().getTokenValue());
	}

	@Test
	public void messageToHeadersTest_ContractAgreementMessage() {
		Message message = UtilMessageService.getContractAgreementMessage();

		Map<String, Object> headers = HttpHeadersUtil.messageToHttpHeaders(message);

		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype"), "ids:ContractAgreementMessage");
		assertEquals(headers.get("IDS-Id"), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue"), message.getSecurityToken().getTokenValue());
	}

	@Test
	public void messageToHeadersTest_ArtifactRequestMessage() {
		Message message = UtilMessageService.getArtifactRequestMessage();

		Map<String, Object> headers = HttpHeadersUtil.messageToHttpHeaders(message);

		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype"), "ids:ArtifactRequestMessage");
		assertEquals(headers.get("IDS-Id"), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue"), message.getSecurityToken().getTokenValue());
	}

	@Test
	public void headersToMessage_DescriptionRequestMessage() {
		htppHeaders.add("ids-id",
				"https://w3id.org/idsa/autogen/descriptionRequestMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");

		Message httpHeadersToMessage = HttpHeadersUtil.httpHeadersToMessage(htppHeaders);

		assertNotNull(httpHeadersToMessage);
		assertEquals(htppHeaders.get("ids-id").get(0), httpHeadersToMessage.getId().toString());
	}

	@Test
	public void headersToMessage_ContractRequestMessage() {
		htppHeaders.add("ids-id",
				"https://w3id.org/idsa/autogen/contractRequestMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");

		Message httpHeadersToMessage = HttpHeadersUtil.httpHeadersToMessage(htppHeaders);

		assertNotNull(httpHeadersToMessage);
		assertEquals(htppHeaders.get("ids-id").get(0), httpHeadersToMessage.getId().toString());
	}

	@Test
	public void headersToMessage_ContractAgreementMessage() {
		htppHeaders.add("ids-id",
				"https://w3id.org/idsa/autogen/contractAgreementMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");

		Message httpHeadersToMessage = HttpHeadersUtil.httpHeadersToMessage(htppHeaders);

		assertNotNull(httpHeadersToMessage);
		assertEquals(htppHeaders.get("ids-id").get(0), httpHeadersToMessage.getId().toString());
	}

	@Test
	public void headersToMessage_ArtifactRequestMessage() {
		htppHeaders.add("ids-id",
				"https://w3id.org/idsa/autogen/artifactRequestMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");

		Message httpHeadersToMessage = HttpHeadersUtil.httpHeadersToMessage(htppHeaders);

		assertNotNull(httpHeadersToMessage);
		assertEquals(htppHeaders.get("ids-id").get(0), httpHeadersToMessage.getId().toString());
	}

	@Test
	public void createResponseMessageHeadersTest() {

		Message message = UtilMessageService.getArtifactRequestMessage();

		Map<String, Object> httpHeadersToMap = HttpHeadersUtil.messageToHttpHeaders(message);

		HttpHeaders messageToHeaders = HttpHeadersUtil.createResponseMessageHttpHeaders(httpHeadersToMap);

		assertNotNull(messageToHeaders);
		assertEquals(message.getId().toString(), messageToHeaders.get("ids-id").get(0));

	}

}

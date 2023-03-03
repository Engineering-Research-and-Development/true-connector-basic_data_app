package it.eng.idsa.dataapp.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.Message;

import it.eng.idsa.multipart.util.UtilMessageService;

class HttpHeadersUtilTest {

	@InjectMocks
	HttpHeadersUtil headersUtil;
	private HttpHeaders htppHeaders;

	@BeforeEach
	public void init() {

		htppHeaders = new HttpHeaders();
		htppHeaders.add("ids-issued", UtilMessageService.ISSUED.toString());
		htppHeaders.add("ids-issuerconnector", UtilMessageService.ISSUER_CONNECTOR.toString());
		htppHeaders.add("ids-correlationmessage", UtilMessageService.CORRELATION_MESSAGE.toString());
		htppHeaders.add("ids-transfercontract", UtilMessageService.TRANSFER_CONTRACT.toString());
		htppHeaders.add("ids-modelversion", UtilMessageService.MODEL_VERSION);
		htppHeaders.add("ids-securitytoken-tokenvalue", UtilMessageService.getDynamicAttributeToken().getTokenValue());
		htppHeaders.add("ids-securitytoken-tokenformat",
				UtilMessageService.getDynamicAttributeToken().getTokenFormat().toString());
		htppHeaders.add("ids-securitytoken-type", "ids:DynamicAttributeToken");
		htppHeaders.add("ids-securitytoken-id",
				"https://w3id.org/idsa/autogen/dynamicAttributeToken/5f6ed0a4-417f-47bf-82eb-b70cd468e702");
		htppHeaders.add("foo", "bar");
		htppHeaders.add("Forward-To", "https://forwardToURL");
	}

	@Test
	public void messageToHeadersTest_DescriptionRequestMessage() {
		Message message = UtilMessageService.getDescriptionRequestMessage(null);

		HttpHeaders headers = HttpHeadersUtil.messageToHttpHeaders(message);

		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype").get(0), "ids:DescriptionRequestMessage");
		assertEquals(headers.get("IDS-Id").get(0), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue").get(0), message.getSecurityToken().getTokenValue());
	}

	@Test
	public void messageToHeadersTest_ContractRequestMessage() {
		Message message = UtilMessageService.getContractRequestMessage();

		HttpHeaders headers = HttpHeadersUtil.messageToHttpHeaders(message);

		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype").get(0), "ids:ContractRequestMessage");
		assertEquals(headers.get("IDS-Id").get(0), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue").get(0), message.getSecurityToken().getTokenValue());
	}

	@Test
	public void messageToHeadersTest_ContractAgreementMessage() {
		Message message = UtilMessageService.getContractAgreementMessage();

		HttpHeaders headers = HttpHeadersUtil.messageToHttpHeaders(message);

		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype").get(0), "ids:ContractAgreementMessage");
		assertEquals(headers.get("IDS-Id").get(0), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue").get(0), message.getSecurityToken().getTokenValue());
	}

	@Test
	public void messageToHeadersTest_ArtifactRequestMessage() {
		Message message = UtilMessageService.getArtifactRequestMessage();

		HttpHeaders headers = HttpHeadersUtil.messageToHttpHeaders(message);

		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype").get(0), "ids:ArtifactRequestMessage");
		assertEquals(headers.get("IDS-Id").get(0), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue").get(0), message.getSecurityToken().getTokenValue());
	}

	@Test
	public void headersToMessage_DescriptionRequestMessage() {
		// Add specific message type
		htppHeaders.add("ids-messagetype", "ids:DescriptionRequestMessage");
		htppHeaders.add("ids-id",
				"https://w3id.org/idsa/autogen/descriptionRequestMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");

		// Store values before they are deleted in httpHeadersToMessage method
		String idsSecurityTokenValue = htppHeaders.get("ids-securitytoken-tokenvalue").get(0);
		String idsSecurityTokenFormat = htppHeaders.get("ids-securitytoken-tokenformat").get(0);

		Message descriptionRequestMessage = HttpHeadersUtil.httpHeadersToMessage(htppHeaders);

		assertNotNull(descriptionRequestMessage);
		assertTrue(descriptionRequestMessage.getClass().getSimpleName().replace("Impl", "")
				.equals(DescriptionRequestMessage.class.getSimpleName()));
		assertEquals(htppHeaders.get("ids-issuerconnector").get(0),
				descriptionRequestMessage.getIssuerConnector().toString());
		assertEquals(htppHeaders.get("ids-issued").get(0), descriptionRequestMessage.getIssued().toString());
		assertEquals(idsSecurityTokenFormat, descriptionRequestMessage.getSecurityToken().getTokenFormat().toString());
		assertEquals(idsSecurityTokenValue, descriptionRequestMessage.getSecurityToken().getTokenValue().toString());
		assertEquals(htppHeaders.get("ids-id").get(0), descriptionRequestMessage.getId().toString());
	}

	@Test
	public void headersToMessage_ContractRequestMessage() {
		// Add specific message type
		htppHeaders.add("ids-messagetype", "ids:ContractRequestMessage");
		htppHeaders.add("ids-id",
				"https://w3id.org/idsa/autogen/contractRequestMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");

		// Store values before they are deleted in httpHeadersToMessage method
		String idsSecurityTokenValue = htppHeaders.get("ids-securitytoken-tokenvalue").get(0);
		String idsSecurityTokenFormat = htppHeaders.get("ids-securitytoken-tokenformat").get(0);

		Message contractRequestMessage = HttpHeadersUtil.httpHeadersToMessage(htppHeaders);

		assertNotNull(contractRequestMessage);
		assertTrue(contractRequestMessage.getClass().getSimpleName().replace("Impl", "")
				.equals(ContractRequestMessage.class.getSimpleName()));
		assertEquals(htppHeaders.get("ids-issuerconnector").get(0),
				contractRequestMessage.getIssuerConnector().toString());
		assertEquals(htppHeaders.get("ids-issued").get(0), contractRequestMessage.getIssued().toString());
		assertEquals(idsSecurityTokenFormat, contractRequestMessage.getSecurityToken().getTokenFormat().toString());
		assertEquals(idsSecurityTokenValue, contractRequestMessage.getSecurityToken().getTokenValue().toString());
		assertEquals(htppHeaders.get("ids-id").get(0), contractRequestMessage.getId().toString());
	}

	@Test
	public void headersToMessage_ContractAgreementMessage() {
		// Add specific message type
		htppHeaders.add("ids-messagetype", "ids:ContractAgreementMessage");
		htppHeaders.add("ids-id",
				"https://w3id.org/idsa/autogen/contractAgreementMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");

		// Store values before they are deleted in httpHeadersToMessage method
		String idsSecurityTokenValue = htppHeaders.get("ids-securitytoken-tokenvalue").get(0);
		String idsSecurityTokenFormat = htppHeaders.get("ids-securitytoken-tokenformat").get(0);

		Message contractAgreementMessage = HttpHeadersUtil.httpHeadersToMessage(htppHeaders);

		assertNotNull(contractAgreementMessage);
		assertTrue(contractAgreementMessage.getClass().getSimpleName().replace("Impl", "")
				.equals(ContractAgreementMessage.class.getSimpleName()));
		assertEquals(htppHeaders.get("ids-issuerconnector").get(0),
				contractAgreementMessage.getIssuerConnector().toString());
		assertEquals(htppHeaders.get("ids-issued").get(0), contractAgreementMessage.getIssued().toString());
		assertEquals(idsSecurityTokenFormat, contractAgreementMessage.getSecurityToken().getTokenFormat().toString());
		assertEquals(idsSecurityTokenValue, contractAgreementMessage.getSecurityToken().getTokenValue().toString());
		assertEquals(htppHeaders.get("ids-id").get(0), contractAgreementMessage.getId().toString());
	}

	@Test
	public void headersToMessage_ArtifactRequestMessage() {
		// Add specific message type
		htppHeaders.add("ids-messagetype", "ids:ArtifactRequestMessage");
		htppHeaders.add("ids-id",
				"https://w3id.org/idsa/autogen/artifactRequestMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");

		// Store values before they are deleted in httpHeadersToMessage method
		String idsSecurityTokenValue = htppHeaders.get("ids-securitytoken-tokenvalue").get(0);
		String idsSecurityTokenFormat = htppHeaders.get("ids-securitytoken-tokenformat").get(0);
		Message artifactRequestMessage = HttpHeadersUtil.httpHeadersToMessage(htppHeaders);

		assertNotNull(artifactRequestMessage);
		assertTrue(artifactRequestMessage.getClass().getSimpleName().replace("Impl", "")
				.equals(ArtifactRequestMessage.class.getSimpleName()));
		assertEquals(htppHeaders.get("ids-issuerconnector").get(0),
				artifactRequestMessage.getIssuerConnector().toString());
		assertEquals(htppHeaders.get("ids-issued").get(0), artifactRequestMessage.getIssued().toString());
		assertEquals(idsSecurityTokenFormat, artifactRequestMessage.getSecurityToken().getTokenFormat().toString());
		assertEquals(idsSecurityTokenValue, artifactRequestMessage.getSecurityToken().getTokenValue().toString());
		assertEquals(htppHeaders.get("ids-id").get(0), artifactRequestMessage.getId().toString());
	}
}

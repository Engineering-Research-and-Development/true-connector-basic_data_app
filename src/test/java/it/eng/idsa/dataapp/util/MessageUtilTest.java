package it.eng.idsa.dataapp.util;

import static de.fraunhofer.iais.eis.util.Util.asList;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClientException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.util.UtilMessageService;

public class MessageUtilTest {

	private MessageUtil messageUtil;

	private String issuerConnector = "http://w3id.org/engrd/connector/";

	@BeforeEach
	public void init() throws RestClientException, IOException {
		MockitoAnnotations.openMocks(this);
		messageUtil = new MessageUtil(issuerConnector);

	}

	@Test
	public void bigPayload() throws UnsupportedOperationException, IOException {
		String header = UtilMessageService.getMessageAsString(UtilMessageService.getArtifactResponseMessage());

		HttpEntity httpEntity = messageUtil.createMultipartMessageForm(header, BigPayload.BIG_PAYLOAD,
				ContentType.TEXT_PLAIN);
		assertNotNull(httpEntity);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		httpEntity.writeTo(outStream);
		outStream.flush();

		String bigMultipartResponse = new String(outStream.toByteArray(), StandardCharsets.UTF_8);
		assertNotNull(bigMultipartResponse);
	}

	@Test
	public void createMultipartMessageFormContentTypeNullTest() throws UnsupportedOperationException, IOException {
		String header = UtilMessageService.getMessageAsString(UtilMessageService.getArtifactResponseMessage());

		HttpEntity httpEntity = messageUtil.createMultipartMessageForm(header, createResponsePayload(), null);
		assertNotNull(httpEntity);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		httpEntity.writeTo(outStream);
		outStream.flush();

		String genericResponse = new String(outStream.toByteArray(), StandardCharsets.UTF_8);
		assertNotNull(genericResponse);
	}

	@Test
	public void createMultipartMessageFormContentTypeNullJsonNonValidTest()
			throws UnsupportedOperationException, IOException {
		String header = UtilMessageService.getMessageAsString(UtilMessageService.getArtifactResponseMessage());

		HttpEntity httpEntity = messageUtil.createMultipartMessageForm(header, BigPayload.BIG_PAYLOAD, null);
		assertNotNull(httpEntity);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		httpEntity.writeTo(outStream);
		outStream.flush();

		String bigMultipartResponse = new String(outStream.toByteArray(), StandardCharsets.UTF_8);
		assertNotNull(bigMultipartResponse);
	}

	@Test
	public void createRejectionMessageTest() {
		Message message = UtilMessageService.getArtifactRequestMessage();
		Message rejectionMessage = messageUtil.createRejectionMessage(message);

		assertNotNull(rejectionMessage);
		assertEquals(asList(message.getIssuerConnector()), rejectionMessage.getRecipientConnector());
		assertEquals(message.getId(), rejectionMessage.getCorrelationMessage());
		assertNotNull(rejectionMessage.getSecurityToken());
	}

	private String createResponsePayload() {
		// Put check sum in the payload
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String formattedDate = dateFormat.format(date);

		Map<String, String> jsonObject = new HashMap<>();
		jsonObject.put("firstName", "John");
		jsonObject.put("lastName", "Doe");
		jsonObject.put("dateOfBirth", formattedDate);
		jsonObject.put("address", "591  Franklin Street, Pennsylvania");
		jsonObject.put("checksum", "ABC123 " + formattedDate);
		Gson gson = new GsonBuilder().create();

		return gson.toJson(jsonObject);
	}
}

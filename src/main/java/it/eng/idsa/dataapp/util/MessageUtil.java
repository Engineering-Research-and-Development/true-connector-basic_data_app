package it.eng.idsa.dataapp.util;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.net.URI;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

@Component
public class MessageUtil {
	private static final Logger logger = LoggerFactory.getLogger(MessageUtil.class);

	private String issueConnector;

	public MessageUtil(@Value("${application.ecc.issuer.connector}") String issuerConnector) {
		super();
		this.issueConnector = issuerConnector;
	}

	public Message createRejectionMessage(Message header) {
		return new RejectionMessageBuilder()._issuerConnector_(whoIAmEngRDProvider())._issued_(DateUtil.normalizedDateTime())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._rejectionReason_(RejectionReason.MALFORMED_MESSAGE)
				._securityToken_(UtilMessageService.getDynamicAttributeToken())._senderAgent_(whoIAmEngRDProvider())
				.build();
	}

	/**
	 * Create HttpEntity from header and payload
	 * 
	 * @param header             Header part
	 * @param payload            Payload part
	 * @param payloadContentType if is null, using default - application/json,
	 *                           otherwise using the one that is passed as in param
	 * @return HttpEntity
	 */
	public HttpEntity createMultipartMessageForm(String header, String payload, ContentType payloadContentType) {
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setStrictMode();

		ContentType payloadCT = ContentType.TEXT_PLAIN;

		if (payloadContentType == null) {
			if (isValidJSON(payload)) {
				payloadCT = ContentType.APPLICATION_JSON;
			}
		} else {
			payloadCT = payloadContentType;
		}

		try {
			FormBodyPart bodyHeaderPart;
			ContentBody headerBody = new StringBody(header, ContentType.create("application/ld+json"));
			bodyHeaderPart = FormBodyPartBuilder.create("header", headerBody).build();
			bodyHeaderPart.addField(HTTP.CONTENT_LEN, "" + header.length());
			multipartEntityBuilder.addPart(bodyHeaderPart);

			FormBodyPart bodyPayloadPart = null;
			if (payload != null) {
				ContentBody payloadBody = new StringBody(payload, payloadCT);
				bodyPayloadPart = FormBodyPartBuilder.create("payload", payloadBody).build();
				bodyPayloadPart.addField(HTTP.CONTENT_LEN, "" + payload.length());
				multipartEntityBuilder.addPart(bodyPayloadPart);
			}

		} catch (Exception e) {
			logger.error("Error while creating response ", e);
		}
		return multipartEntityBuilder.build();
	}

	public boolean isValidJSON(String json) {
		try {
			JsonParser.parseString(json);
		} catch (JsonSyntaxException e) {
			return false;
		}
		return true;
	}

	public static String getIDSMessageType(Message message) {
		return message.getClass().getSimpleName().substring(0, message.getClass().getSimpleName().lastIndexOf("Impl"));
	}
	
	public static MultiValueMap<String, String> REMOVE_IDS_MESSAGE_HEADERS(HttpHeaders headers) {
		MultiValueMap<String, String> newHeaders = new LinkedMultiValueMap<>();
		newHeaders.putAll(headers);
		for (Iterator<String> iterator = newHeaders.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			// String.contains is case sensitive so this should have minimal margin of error
			if (key.contains("IDS-")) {
				iterator.remove();
			}
		}
		return newHeaders;
	}

	private URI whoIAm() {
		return URI.create("http://auto-generated");
	}

	private URI whoIAmEngRDProvider() {
		return URI.create(issueConnector);
	}

}

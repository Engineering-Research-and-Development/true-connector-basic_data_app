package it.eng.idsa.dataapp.web.rest.exceptions;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.UUID;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.TokenFormat;
import it.eng.idsa.dataapp.util.HttpHeadersUtil;
import it.eng.idsa.dataapp.util.MessageUtil;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

@Order(Ordered.LOWEST_PRECEDENCE)
@ControllerAdvice
public class DataAppExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(DataAppExceptionHandler.class);

	private String issuerConnector;
	private String httpConfig;
	private MessageUtil messageUtil;
	private HttpHeadersUtil httpHeadersUtil;

	public DataAppExceptionHandler(@Value("${application.ecc.issuer.connector}") String issuerConnector,
			@Value("${application.dataapp.http.config}") String httpConfig, MessageUtil messageUtil,
			HttpHeadersUtil httpHeadersUtil) {
		super();
		this.issuerConnector = issuerConnector;
		this.httpConfig = httpConfig;
		this.messageUtil = messageUtil;
		this.httpHeadersUtil = httpHeadersUtil;
	}

	@ExceptionHandler(InternalRecipientException.class)
	protected ResponseEntity<Object> handleInternalRecipientException(InternalRecipientException ex,
			HttpServletRequest req) throws IOException {

		logger.info("handleInternalRecipientException");
		logger.error("Message: " + ex.getMessage());

		Message errorMessage = createErrorMessage(ex.getHeader(), RejectionReason.INTERNAL_RECIPIENT_ERROR);

		return buildErrorResponse(errorMessage);
	}

	@ExceptionHandler(NotFoundException.class)
	protected ResponseEntity<Object> handleNotFoundException(NotFoundException ex, HttpServletRequest req)
			throws IOException {

		logger.info("handleNotFoundException");
		logger.error("Message: " + ex.getMessage());

		Message errorMessage = createErrorMessage(ex.getHeader(), RejectionReason.NOT_FOUND);

		return buildErrorResponse(errorMessage);
	}

	@ExceptionHandler(MalformedMessageException.class)
	protected ResponseEntity<Object> handleMalformedMessageException(MalformedMessageException ex,
			HttpServletRequest req) throws IOException {

		logger.info("handleMalformedMessageException");
		logger.error("Message: " + ex.getMessage());

		Message errorMessage = createErrorMessage(ex.getHeader(), RejectionReason.MALFORMED_MESSAGE);

		return buildErrorResponse(errorMessage);
	}

	@ExceptionHandler(NotAuthenticatedException.class)
	protected ResponseEntity<Object> handleNotAuthenticatedException(NotAuthenticatedException ex,
			HttpServletRequest req) throws IOException {

		logger.info("handleNotAuthenticatedException");
		logger.error("Message: " + ex.getMessage());

		Message errorMessage = createErrorMessage(ex.getHeader(), RejectionReason.NOT_AUTHENTICATED);

		return buildErrorResponse(errorMessage);
	}

	@SuppressWarnings("unchecked")
	private ResponseEntity<Object> buildErrorResponse(Message header) throws IOException {
		if (StringUtils.equals("http-header", httpConfig)) {
			HttpHeaders responseHeaders = new HttpHeaders();
			Map<String, Object> responseHeaderMap = new HashMap<>();
			responseHeaderMap = httpHeadersUtil.messageToHeaders(header);
			responseHeaders = httpHeadersUtil.createResponseMessageHeaders(responseHeaderMap);

			ResponseEntity<?> response = ResponseEntity.noContent().headers(responseHeaders).build();

			return (ResponseEntity<Object>) response;
		} else {
			ContentType payloadContentType = ContentType.TEXT_PLAIN;

			// prepare body response - multipart message.
			HttpEntity resultEntity = messageUtil.createMultipartMessageForm(
					MultipartMessageProcessor.serializeToJsonLD(header), null, payloadContentType);

			String contentType = resultEntity.getContentType().getValue();
			contentType = contentType.replace("multipart/form-data", "multipart/mixed");

			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			resultEntity.writeTo(outStream);
			outStream.flush();

			return ResponseEntity.ok().header("foo", "bar")
					.contentType(MediaType.parseMediaType(resultEntity.getContentType().getValue()))
					.body(outStream.toString());
		}
	}

	private Message createErrorMessage(Message header, RejectionReason rejectionReason) {

		if (StringUtils.equals("http-header", httpConfig)) {

			HttpHeaders headers = new HttpHeaders();
			String responseMessageType = "ids:RejectionMessage";
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			Date date = new Date();
			String formattedDate = dateFormat.format(date);

			headers.add("ids-messagetype", responseMessageType);
			headers.add("ids-issued", formattedDate);
			headers.add("ids-issuerconnector", issuerConnector);
			headers.add("ids-correlationmessage", header != null ? header.getId().toString() : whoIAm().toString());
			headers.add("ids-modelversion", UtilMessageService.MODEL_VERSION);
			headers.add("ids-id",
					"https://w3id.org/idsa/autogen/" + responseMessageType + "/" + UUID.randomUUID().toString());
			headers.add("ids-senderagent", "https://sender.agent.com");
			headers.add("ids-securitytoken-type", "ids:DynamicAttributeToken");
			headers.add("ids-securitytoken-id", "https://w3id.org/idsa/autogen/" + UUID.randomUUID());
			headers.add("ids-securitytoken-tokenformat", TokenFormat.JWT.getId().toString());
			headers.add("ids-securitytoken-tokenvalue", UtilMessageService.TOKEN_VALUE);
			headers.add("ids-rejectionreason", rejectionReason.toString());

			headers.add("foo", "bar");
			Map<String, Object> headersMap = httpHeadersUtil.httpHeadersToMap(headers);

			return httpHeadersUtil.headersToMessage(headersMap);
		} else {

			return new RejectionMessageBuilder()._issuerConnector_(whoIAmEngRDProvider())._issued_(DateUtil.now())
					._modelVersion_(UtilMessageService.MODEL_VERSION)
					._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
					._correlationMessage_(header != null ? header.getId() : whoIAm())._rejectionReason_(rejectionReason)
					._securityToken_(UtilMessageService.getDynamicAttributeToken())._senderAgent_(whoIAmEngRDProvider())
					.build();
		}
	}

	protected URI whoIAm() {
		return URI.create("http://auto-generated");
	}

	protected URI whoIAmEngRDProvider() {
		return URI.create(issuerConnector);
	}
}

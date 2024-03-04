package it.eng.idsa.dataapp.web.rest.exceptions;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
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

	public DataAppExceptionHandler(@Value("${application.ecc.issuer.connector}") String issuerConnector,
			@Value("${application.dataapp.http.config}") String httpConfig, MessageUtil messageUtil) {
		super();
		this.issuerConnector = issuerConnector;
		this.httpConfig = httpConfig;
		this.messageUtil = messageUtil;
	}

	@ExceptionHandler(CertificateMissingException.class)
	protected ResponseEntity<String> handleCertificateMissingException(CertificateMissingException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(BadParametersException.class)
	protected ResponseEntity<Object> handleBadParametersException(BadParametersException ex, HttpServletRequest req)
			throws IOException {

		logger.info("handleBadParametersException");
		logger.error("Message: " + ex.getMessage());

		Message errorMessage = createErrorMessage(ex.getHeader(), RejectionReason.BAD_PARAMETERS);

		return buildErrorResponse(errorMessage);
	}

	@ExceptionHandler(InternalRecipientException.class)
	protected ResponseEntity<Object> handleInternalRecipientException(InternalRecipientException ex,
			HttpServletRequest req) throws IOException {

		logger.info("handleInternalRecipientException");
		logger.error("Message: " + ex.getMessage());

		Message errorMessage = createErrorMessage(ex.getHeader(), RejectionReason.INTERNAL_RECIPIENT_ERROR);

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

	@ExceptionHandler(MessageTypeNotSupportedException.class)
	protected ResponseEntity<Object> handleMessageTypeNotSupportedException(MessageTypeNotSupportedException ex,
			HttpServletRequest req) throws IOException {

		logger.info("handleMessageTypeNotSupportedException");
		logger.error("Message: " + ex.getMessage());

		Message errorMessage = createErrorMessage(ex.getHeader(), RejectionReason.MESSAGE_TYPE_NOT_SUPPORTED);

		return buildErrorResponse(errorMessage);
	}

	@ExceptionHandler(MethodNotSupportedException.class)
	protected ResponseEntity<Object> handleMethodTypeNotSupportedException(MethodNotSupportedException ex,
			HttpServletRequest req) throws IOException {

		logger.info("handleMethodTypeNotSupportedException");
		logger.error("Message: " + ex.getMessage());

		Message errorMessage = createErrorMessage(ex.getHeader(), RejectionReason.METHOD_NOT_SUPPORTED);

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

	@ExceptionHandler(NotAuthorizedException.class)
	protected ResponseEntity<Object> handleNotAuthorizeddException(NotAuthorizedException ex, HttpServletRequest req)
			throws IOException {

		logger.info("handleNotAuthorizeddException");
		logger.error("Message: " + ex.getMessage());

		Message errorMessage = createErrorMessage(ex.getHeader(), RejectionReason.NOT_AUTHORIZED);

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

	@ExceptionHandler(TemporarilyNotAvailableException.class)
	protected ResponseEntity<Object> handleTemporarilyNotAvailableException(TemporarilyNotAvailableException ex,
			HttpServletRequest req) throws IOException {

		logger.info("handleTemporarilyNotAvailableException");
		logger.error("Message: " + ex.getMessage());

		Message errorMessage = createErrorMessage(ex.getHeader(), RejectionReason.TEMPORARILY_NOT_AVAILABLE);

		return buildErrorResponse(errorMessage);
	}

	@ExceptionHandler(TooManyResultsException.class)
	protected ResponseEntity<Object> handleTooManyResultsException(TooManyResultsException ex, HttpServletRequest req)
			throws IOException {

		logger.info("handleTooManyResultsException");
		logger.error("Message: " + ex.getMessage());

		Message errorMessage = createErrorMessage(ex.getHeader(), RejectionReason.TOO_MANY_RESULTS);

		return buildErrorResponse(errorMessage);
	}

	@ExceptionHandler(VersionNotSupportedException.class)
	protected ResponseEntity<Object> handleVersionNotSupportedException(VersionNotSupportedException ex,
			HttpServletRequest req) throws IOException {

		logger.info("handleVersionNotSupportedException");
		logger.error("Message: " + ex.getMessage());

		Message errorMessage = createErrorMessage(ex.getHeader(), RejectionReason.VERSION_NOT_SUPPORTED);

		return buildErrorResponse(errorMessage);
	}

	@SuppressWarnings("unchecked")
	private ResponseEntity<Object> buildErrorResponse(Message header) throws IOException {
		if (StringUtils.equals("http-header", httpConfig)) {
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders = HttpHeadersUtil.messageToHttpHeaders(header);

			ResponseEntity<?> response = ResponseEntity.noContent().headers(responseHeaders).build();

			return (ResponseEntity<Object>) response;
		} else {
			ContentType payloadContentType = ContentType.TEXT_PLAIN;

			// prepare body response - multipart message.
			HttpEntity resultEntity = messageUtil.createMultipartMessageForm(
					MultipartMessageProcessor.serializeToJsonLD(header), null, payloadContentType);

			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			resultEntity.writeTo(outStream);
			outStream.flush();

			return ResponseEntity.ok().header("foo", "bar")
					.contentType(MediaType.parseMediaType(resultEntity.getContentType().getValue()))
					.body(outStream.toString());
		}
	}

	private Message createErrorMessage(Message header, RejectionReason rejectionReason) {

		return new RejectionMessageBuilder()._issuerConnector_(whoIAmEngRDProvider())
				._issued_(DateUtil.normalizedDateTime())._modelVersion_(UtilMessageService.MODEL_VERSION)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())._rejectionReason_(rejectionReason)
				._securityToken_(UtilMessageService.getDynamicAttributeToken())._senderAgent_(whoIAmEngRDProvider())
				.build();

	}

	protected URI whoIAm() {

		return URI.create("http://auto-generated");
	}

	protected URI whoIAmEngRDProvider() {

		return URI.create(issuerConnector);
	}
}

package it.eng.idsa.dataapp.web.rest.exceptions;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.util.MessageUtil;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.UtilMessageService;

class DataAppExceptionHandlerTest {

	private DataAppExceptionHandler dataAppExceptionHandler;
	@Mock
	private MessageUtil messageUtil;
	@Mock
	private HttpEntity resultEntity;
	@Mock
	private MediaType mediaType;
	@Mock
	private Header contentTypeHeader;
	private Message header;
	private String issuerConnector = "http://w3id.org/engrd/connector/";
	private String httpConfigHeader = "http-header";
	private String httpConfigForm = "form";

	@BeforeEach
	public void init() {

		MockitoAnnotations.openMocks(this);
		header = UtilMessageService.getArtifactRequestMessage();

	}

	@Test
	public void handleFormExceptionsTest() throws IOException {
		dataAppExceptionHandler = new DataAppExceptionHandler(issuerConnector, httpConfigForm, messageUtil);

		when(messageUtil.createMultipartMessageForm(MultipartMessageProcessor.serializeToJsonLD(any()), any(), any()))
				.thenReturn(resultEntity);
		when(resultEntity.getContentType()).thenReturn(contentTypeHeader);
		when(resultEntity.getContentType().getValue()).thenReturn("text/plain");
		BadParametersException badParametersException = new BadParametersException("BadParametersException", header);
		InternalRecipientException internalRecipientException = new InternalRecipientException(
				"InternalRecipientException", header);
		MalformedMessageException malformedMessageException = new MalformedMessageException("MalformedMessageException",
				header);
		MessageTypeNotSupportedException messageTypeNotSupportedException = new MessageTypeNotSupportedException(
				"MessageTypeNotSupportedException", header);
		MethodNotSupportedException methodNotSupportedException = new MethodNotSupportedException(
				"MethodNotSupportedException", header);
		NotAuthenticatedException notAuthenticatedException = new NotAuthenticatedException("NotAuthenticatedException",
				header);
		NotAuthorizedException notAuthorizedException = new NotAuthorizedException("NotAuthorizedException", header);
		NotFoundException notFoundException = new NotFoundException("NotFoundException", header);
		TemporarilyNotAvailableException temporarilyNotAvailableException = new TemporarilyNotAvailableException(
				"TemporarilyNotAvailableException", header);
		TooManyResultsException tooManyResultsException = new TooManyResultsException("TooManyResultsException",
				header);
		VersionNotSupportedException versionNotSupportedException = new VersionNotSupportedException(
				"VersionNotSupportedException", header);

		dataAppExceptionHandler.handleBadParametersException(badParametersException, null);
		dataAppExceptionHandler.handleInternalRecipientException(internalRecipientException, null);
		dataAppExceptionHandler.handleMalformedMessageException(malformedMessageException, null);
		dataAppExceptionHandler.handleMessageTypeNotSupportedException(messageTypeNotSupportedException, null);
		dataAppExceptionHandler.handleMethodTypeNotSupportedException(methodNotSupportedException, null);
		dataAppExceptionHandler.handleNotAuthenticatedException(notAuthenticatedException, null);
		dataAppExceptionHandler.handleNotAuthorizeddException(notAuthorizedException, null);
		dataAppExceptionHandler.handleNotFoundException(notFoundException, null);
		dataAppExceptionHandler.handleTemporarilyNotAvailableException(temporarilyNotAvailableException, null);
		dataAppExceptionHandler.handleTooManyResultsException(tooManyResultsException, null);
		dataAppExceptionHandler.handleVersionNotSupportedException(versionNotSupportedException, null);

		assertNotNull(badParametersException);
		assertNotNull(internalRecipientException);
		assertNotNull(malformedMessageException);
		assertNotNull(messageTypeNotSupportedException);
		assertNotNull(methodNotSupportedException);
		assertNotNull(notAuthenticatedException);
		assertNotNull(notAuthorizedException);
		assertNotNull(notFoundException);
		assertNotNull(temporarilyNotAvailableException);
		assertNotNull(tooManyResultsException);
		assertNotNull(versionNotSupportedException);
	}

	@Test
	public void testHandleHttpHeadersExceptions() throws IOException {
		dataAppExceptionHandler = new DataAppExceptionHandler(issuerConnector, httpConfigHeader, messageUtil);
		BadParametersException badParametersException = new BadParametersException("BadParametersException", header);
		InternalRecipientException internalRecipientException = new InternalRecipientException(
				"InternalRecipientException", header);
		MalformedMessageException malformedMessageException = new MalformedMessageException("MalformedMessageException",
				header);
		MessageTypeNotSupportedException messageTypeNotSupportedException = new MessageTypeNotSupportedException(
				"MessageTypeNotSupportedException", header);
		MethodNotSupportedException methodNotSupportedException = new MethodNotSupportedException(
				"MethodNotSupportedException", header);
		NotAuthenticatedException notAuthenticatedException = new NotAuthenticatedException("NotAuthenticatedException",
				header);
		NotAuthorizedException notAuthorizedException = new NotAuthorizedException("NotAuthorizedException", header);
		NotFoundException notFoundException = new NotFoundException("NotFoundException", header);
		TemporarilyNotAvailableException temporarilyNotAvailableException = new TemporarilyNotAvailableException(
				"TemporarilyNotAvailableException", header);
		TooManyResultsException tooManyResultsException = new TooManyResultsException("TooManyResultsException",
				header);
		VersionNotSupportedException versionNotSupportedException = new VersionNotSupportedException(
				"VersionNotSupportedException", header);

		dataAppExceptionHandler.handleBadParametersException(badParametersException, null);
		dataAppExceptionHandler.handleInternalRecipientException(internalRecipientException, null);
		dataAppExceptionHandler.handleMalformedMessageException(malformedMessageException, null);
		dataAppExceptionHandler.handleMessageTypeNotSupportedException(messageTypeNotSupportedException, null);
		dataAppExceptionHandler.handleMethodTypeNotSupportedException(methodNotSupportedException, null);
		dataAppExceptionHandler.handleNotAuthenticatedException(notAuthenticatedException, null);
		dataAppExceptionHandler.handleNotAuthorizeddException(notAuthorizedException, null);
		dataAppExceptionHandler.handleNotFoundException(notFoundException, null);
		dataAppExceptionHandler.handleTemporarilyNotAvailableException(temporarilyNotAvailableException, null);
		dataAppExceptionHandler.handleTooManyResultsException(tooManyResultsException, null);
		dataAppExceptionHandler.handleVersionNotSupportedException(versionNotSupportedException, null);

		assertNotNull(badParametersException);
		assertNotNull(internalRecipientException);
		assertNotNull(malformedMessageException);
		assertNotNull(messageTypeNotSupportedException);
		assertNotNull(methodNotSupportedException);
		assertNotNull(notAuthenticatedException);
		assertNotNull(notAuthorizedException);
		assertNotNull(notFoundException);
		assertNotNull(temporarilyNotAvailableException);
		assertNotNull(tooManyResultsException);
		assertNotNull(versionNotSupportedException);
	}
}

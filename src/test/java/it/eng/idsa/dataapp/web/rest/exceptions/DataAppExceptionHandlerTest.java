package it.eng.idsa.dataapp.web.rest.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.util.MessageUtil;

import it.eng.idsa.multipart.util.UtilMessageService;

class DataAppExceptionHandlerTest {

	@InjectMocks
	private DataAppExceptionHandler dataAppExceptionHandler;
	@Mock
	private MessageUtil messageUtil;
	private Message header;
	private String issuerConnector = "http://w3id.org/engrd/connector/";
	private String httpConfig = "form";
	@Mock
	HttpEntity resultEntity;
	private ContentType payloadContentType = ContentType.TEXT_PLAIN;

	@BeforeEach
	public void init() {

		MockitoAnnotations.initMocks(this);
		header = UtilMessageService.getArtifactRequestMessage();
		ReflectionTestUtils.setField(dataAppExceptionHandler, "issuerConnector", issuerConnector);
		ReflectionTestUtils.setField(dataAppExceptionHandler, "httpConfig", httpConfig);

	}

//	@Test
//	public void testHandleBadParametersException() throws IOException {
//		BadParametersException ex = new BadParametersException("test", header);
//		when(messageUtil.createMultipartMessageForm(MultipartMessageProcessor.serializeToJsonLD(header), null,
//				payloadContentType)).thenReturn(resultEntity);
//		dataAppExceptionHandler.handleBadParametersException(ex, null);
//		assertNotNull(ex);
//
//	}

	@Test
	public void testHandleHttpHeadersExceptions() throws IOException {
		ReflectionTestUtils.setField(dataAppExceptionHandler, "httpConfig", "http-header");

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

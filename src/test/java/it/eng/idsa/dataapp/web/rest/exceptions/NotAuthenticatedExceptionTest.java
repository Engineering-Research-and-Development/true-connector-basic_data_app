package it.eng.idsa.dataapp.web.rest.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.util.UtilMessageService;

class NotAuthenticatedExceptionTest {

	private Message header;

	@BeforeEach
	public void init() {
		header = UtilMessageService.getArtifactRequestMessage();
	}

	@Test
	public void testConstructorWithHeader() {
		NotAuthenticatedException exception = new NotAuthenticatedException(header);

		assertEquals(null, exception.getMessage());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testConstructorWithMessage() {
		String message = "Not authenticated";
		NotAuthenticatedException exception = new NotAuthenticatedException(message);

		assertEquals(message, exception.getMessage());
		assertEquals(null, exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndHeader() {
		String message = "Not authenticated";
		NotAuthenticatedException exception = new NotAuthenticatedException(message, header);

		assertEquals(message, exception.getMessage());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndCause() {
		String message = "Not authenticated";
		Throwable cause = new RuntimeException("Cause");
		NotAuthenticatedException exception = new NotAuthenticatedException(message, cause);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertEquals(null, exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndCauseAndHeader() {
		String message = "Not authenticated";
		Throwable cause = new RuntimeException("Cause");
		NotAuthenticatedException exception = new NotAuthenticatedException(message, cause, header);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testSetAndGetHeader() {
		NotAuthenticatedException exception = new NotAuthenticatedException("Not authenticated");

		assertNull(exception.getHeader());
		exception.setHeader(header);
		assertEquals(header, exception.getHeader());
	}
}

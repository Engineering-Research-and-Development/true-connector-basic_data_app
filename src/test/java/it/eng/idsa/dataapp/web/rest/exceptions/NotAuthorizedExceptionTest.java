package it.eng.idsa.dataapp.web.rest.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.util.UtilMessageService;

class NotAuthorizedExceptionTest {
	private Message header;

	@BeforeEach
	public void init() {
		header = UtilMessageService.getArtifactRequestMessage();
	}

	@Test
	void testConstructorWithHeader() {
		NotAuthorizedException ex = new NotAuthorizedException(header);

		assertEquals(header, ex.getHeader());
		assertNull(ex.getMessage());
		assertNull(ex.getCause());
	}

	@Test
	void testConstructorWithMessage() {
		String message = "Not authorized";
		NotAuthorizedException ex = new NotAuthorizedException(message);

		assertEquals(message, ex.getMessage());
		assertNull(ex.getHeader());
		assertNull(ex.getCause());
	}

	@Test
	void testConstructorWithMessageAndHeader() {
		String message = "Not authorized";
		NotAuthorizedException ex = new NotAuthorizedException(message, header);

		assertEquals(message, ex.getMessage());
		assertEquals(header, ex.getHeader());
		assertNull(ex.getCause());
	}

	@Test
	void testConstructorWithMessageAndCause() {
		String message = "Not authorized";
		Throwable cause = new RuntimeException("Cause");
		NotAuthorizedException ex = new NotAuthorizedException(message, cause);

		assertEquals(message, ex.getMessage());
		assertNull(ex.getHeader());
		assertEquals(cause, ex.getCause());
	}

	@Test
	void testConstructorWithMessageCauseAndHeader() {
		String message = "Not authorized";
		Throwable cause = new RuntimeException("Cause");
		NotAuthorizedException ex = new NotAuthorizedException(message, cause, header);

		assertEquals(message, ex.getMessage());
		assertEquals(header, ex.getHeader());
		assertEquals(cause, ex.getCause());
	}

	@Test
	public void testSetAndGetHeader() {
		NotAuthorizedException exception = new NotAuthorizedException("Not authorized");

		assertNull(exception.getHeader());
		exception.setHeader(header);
		assertEquals(header, exception.getHeader());
	}
}

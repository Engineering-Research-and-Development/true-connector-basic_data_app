package it.eng.idsa.dataapp.web.rest.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.util.UtilMessageService;

class TooManyResultsExceptionTest {

	private Message header;

	@BeforeEach
	public void init() {
		header = UtilMessageService.getArtifactRequestMessage();
	}

	@Test
	void testConstructorWithHeader() {
		TooManyResultsException exception = new TooManyResultsException(header);

		assertEquals(header, exception.getHeader());
	}

	@Test
	void testConstructorWithMessage() {
		String message = "Too many results found";
		TooManyResultsException exception = new TooManyResultsException(message);

		assertEquals(message, exception.getMessage());
	}

	@Test
	void testConstructorWithMessageAndCause() {
		String message = "Too many results found";
		Throwable cause = new RuntimeException("Cause");
		TooManyResultsException exception = new TooManyResultsException(message, cause);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
	}

	@Test
	void testConstructorWithMessageAndHeader() {
		String message = "Too many results found";
		TooManyResultsException exception = new TooManyResultsException(message, header);

		assertEquals(message, exception.getMessage());
		assertEquals(header, exception.getHeader());
	}

	@Test
	void testConstructorWithMessageCauseAndHeader() {
		String message = "Too many results found";
		Throwable cause = new RuntimeException("Cause");
		TooManyResultsException exception = new TooManyResultsException(message, cause, header);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testSetAndGetHeader() {
		TooManyResultsException exception = new TooManyResultsException("Too many results found");

		assertNull(exception.getHeader());
		exception.setHeader(header);
		assertEquals(header, exception.getHeader());
	}
}

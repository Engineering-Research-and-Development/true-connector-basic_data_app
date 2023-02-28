package it.eng.idsa.dataapp.web.rest.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.util.UtilMessageService;

class TemporarilyNotAvailableExceptionTest {

	private Message header;

	@BeforeEach
	public void init() {
		header = UtilMessageService.getArtifactRequestMessage();
	}

	@Test
	public void testConstructorWithHeader() {
		TemporarilyNotAvailableException exception = new TemporarilyNotAvailableException(header);

		assertEquals(header, exception.getHeader());
		assertNull(exception.getMessage());
		assertNull(exception.getCause());
	}

	@Test
	public void testConstructorWithMessage() {
		String message = "Temporarily not available";
		TemporarilyNotAvailableException exception = new TemporarilyNotAvailableException(message);

		assertEquals(message, exception.getMessage());
		assertNull(exception.getCause());
		assertNull(exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndCause() {
		String message = "Temporarily not available";
		Throwable cause = new RuntimeException("Cause");
		TemporarilyNotAvailableException exception = new TemporarilyNotAvailableException(message, cause);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertNull(exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndHeader() {
		String message = "Temporarily not available";
		TemporarilyNotAvailableException exception = new TemporarilyNotAvailableException(message, header);

		assertEquals(message, exception.getMessage());
		assertEquals(header, exception.getHeader());
		assertNull(exception.getCause());
	}

	@Test
	public void testConstructorWithMessageCauseAndHeader() {
		String message = "Temporarily not available";
		Throwable cause = new RuntimeException("Cause");
		TemporarilyNotAvailableException exception = new TemporarilyNotAvailableException(message, cause, header);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testSetAndGetHeader() {
		TemporarilyNotAvailableException exception = new TemporarilyNotAvailableException("Temporarily not available");

		assertNull(exception.getHeader());
		exception.setHeader(header);
		assertEquals(header, exception.getHeader());
	}
}

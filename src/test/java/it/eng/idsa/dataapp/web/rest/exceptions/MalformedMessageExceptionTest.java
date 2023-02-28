package it.eng.idsa.dataapp.web.rest.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.util.UtilMessageService;

class MalformedMessageExceptionTest {

	private Message header;

	@BeforeEach
	public void init() {
		header = UtilMessageService.getArtifactRequestMessage();
	}

	@Test
	public void testConstructorWithHeader() {
		MalformedMessageException exception = new MalformedMessageException(header);

		assertEquals(header, exception.getHeader());
		assertNull(exception.getMessage());
		assertNull(exception.getCause());
	}

	@Test
	public void testConstructorWithMessage() {
		String message = "Malformed message error";
		MalformedMessageException exception = new MalformedMessageException(message);

		assertEquals(message, exception.getMessage());
		assertNull(exception.getCause());
		assertNull(exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndHeader() {
		String message = "Malformed message error";
		MalformedMessageException exception = new MalformedMessageException(message, header);

		assertEquals(message, exception.getMessage());
		assertNull(exception.getCause());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndCause() {
		String message = "Malformed message error";
		Throwable cause = new RuntimeException("Cause");
		MalformedMessageException exception = new MalformedMessageException(message, cause);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertNull(exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageCauseAndHeader() {
		String message = "Malformed message error";
		Throwable cause = new RuntimeException("Cause");
		MalformedMessageException exception = new MalformedMessageException(message, cause, header);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testSetAndGetHeader() {
		MalformedMessageException exception = new MalformedMessageException("Malformed message error");

		assertNull(exception.getHeader());
		exception.setHeader(header);
		assertEquals(header, exception.getHeader());
	}
}

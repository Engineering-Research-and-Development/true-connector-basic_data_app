package it.eng.idsa.dataapp.web.rest.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.util.UtilMessageService;

class MessageTypeNotSupportedExceptionTest {

	private Message header;

	@BeforeEach
	public void init() {
		header = UtilMessageService.getArtifactRequestMessage();
	}

	@Test
	public void testConstructorWithHeader() {
		MessageTypeNotSupportedException ex = new MessageTypeNotSupportedException(header);

		assertEquals(header, ex.getHeader());
		assertNull(ex.getMessage());
		assertNull(ex.getCause());
	}

	@Test
	public void testConstructorWithMessage() {
		String message = "Message type not supported error";
		MessageTypeNotSupportedException ex = new MessageTypeNotSupportedException(message);

		assertEquals(message, ex.getMessage());
		assertNull(ex.getCause());
		assertNull(ex.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndHeader() {
		String message = "Message type not supported error";
		MessageTypeNotSupportedException ex = new MessageTypeNotSupportedException(message, header);

		assertEquals(message, ex.getMessage());
		assertNull(ex.getCause());
		assertEquals(header, ex.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndCause() {
		String message = "Message type not supported error";
		Throwable cause = new RuntimeException("Cause");
		MessageTypeNotSupportedException ex = new MessageTypeNotSupportedException(message, cause);

		assertEquals(message, ex.getMessage());
		assertEquals(cause, ex.getCause());
		assertNull(ex.getHeader());
	}

	@Test
	public void testConstructorWithMessageCauseAndHeader() {
		String message = "Message type not supported error";
		Throwable cause = new RuntimeException("Cause");
		MessageTypeNotSupportedException ex = new MessageTypeNotSupportedException(message, cause, header);

		assertEquals(message, ex.getMessage());
		assertEquals(cause, ex.getCause());
		assertEquals(header, ex.getHeader());
	}

	@Test
	public void testSetAndGetHeader() {
		MessageTypeNotSupportedException exception = new MessageTypeNotSupportedException(
				"Message type not supported error");

		assertNull(exception.getHeader());
		exception.setHeader(header);
		assertEquals(header, exception.getHeader());
	}
}

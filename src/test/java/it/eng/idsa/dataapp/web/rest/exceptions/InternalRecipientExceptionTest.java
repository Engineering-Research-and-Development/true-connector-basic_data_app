package it.eng.idsa.dataapp.web.rest.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.util.UtilMessageService;

class InternalRecipientExceptionTest {

	private Message header;

	@BeforeEach
	public void init() {
		header = UtilMessageService.getArtifactRequestMessage();
	}

	@Test
	public void testConstructorWithHeader() {
		InternalRecipientException exception = new InternalRecipientException(header);

		assertEquals(header, exception.getHeader());
		assertNull(exception.getMessage());
		assertNull(exception.getCause());
	}

	@Test
	public void testConstructorWithMessage() {
		String message = "Internal recipient error";
		InternalRecipientException exception = new InternalRecipientException(message);

		assertEquals(message, exception.getMessage());
		assertNull(exception.getCause());
		assertNull(exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndHeader() {
		String message = "Internal recipient error";
		InternalRecipientException exception = new InternalRecipientException(message, header);

		assertEquals(message, exception.getMessage());
		assertNull(exception.getCause());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndCause() {
		String message = "Internal recipient error";
		Throwable cause = new RuntimeException("Cause");
		InternalRecipientException exception = new InternalRecipientException(message, cause);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertNull(exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageCauseAndHeader() {
		String message = "Internal recipient error";
		Throwable cause = new RuntimeException("Cause");
		InternalRecipientException exception = new InternalRecipientException(message, cause, header);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testSetAndGetHeader() {
		InternalRecipientException exception = new InternalRecipientException("Internal recipient error");

		assertNull(exception.getHeader());
		exception.setHeader(header);
		assertEquals(header, exception.getHeader());
	}
}

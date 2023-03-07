package it.eng.idsa.dataapp.web.rest.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.util.UtilMessageService;

class MethodNotSupportedExceptionTest {

	private Message header;

	@BeforeEach
	public void init() {
		header = UtilMessageService.getArtifactRequestMessage();
	}

	@Test
	public void testConstructorWithHeader() {
		MethodNotSupportedException exception = new MethodNotSupportedException(header);

		assertNull(exception.getMessage());
		assertNull(exception.getCause());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testConstructorWithMessage() {
		String message = "Method not supported error";
		MethodNotSupportedException exception = new MethodNotSupportedException(message);

		assertEquals(message, exception.getMessage());
		assertNull(exception.getCause());
		assertNull(exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndHeader() {
		String message = "Method not supported error";
		MethodNotSupportedException exception = new MethodNotSupportedException(message, header);

		assertEquals(message, exception.getMessage());
		assertNull(exception.getCause());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndCause() {
		String message = "Method not supported error";
		Throwable cause = new RuntimeException("Cause");
		MethodNotSupportedException exception = new MethodNotSupportedException(message, cause);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertNull(exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageCauseAndHeader() {
		String message = "Method not supported error";
		Throwable cause = new RuntimeException("Cause");
		MethodNotSupportedException exception = new MethodNotSupportedException(message, cause, header);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testSetAndGetHeader() {
		MethodNotSupportedException exception = new MethodNotSupportedException("Method not supported error");

		assertNull(exception.getHeader());
		exception.setHeader(header);
		assertEquals(header, exception.getHeader());
	}
}

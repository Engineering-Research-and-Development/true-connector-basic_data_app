package it.eng.idsa.dataapp.web.rest.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.util.UtilMessageService;

class NotFoundExceptionTest {

	private Message header;

	@BeforeEach
	public void init() {
		header = UtilMessageService.getArtifactRequestMessage();
	}

	@Test
	public void testConstructorWithMessage() {
		String message = "Resource not found";
		NotFoundException exception = new NotFoundException(message);

		assertEquals(message, exception.getMessage());
	}

	@Test
	public void testConstructorWithHeader() {
		NotFoundException exception = new NotFoundException(header);

		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndHeader() {
		String message = "Resource not found";
		NotFoundException exception = new NotFoundException(message, header);

		assertEquals(message, exception.getMessage());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndCause() {
		String message = "Resource not found";
		Throwable cause = new RuntimeException("Cause");
		NotFoundException exception = new NotFoundException(message, cause);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
	}

	@Test
	public void testConstructorWithMessageCauseAndHeader() {
		String message = "Resource not found";
		Throwable cause = new RuntimeException("Cause");
		NotFoundException exception = new NotFoundException(message, cause, header);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testSetAndGetHeader() {
		NotFoundException exception = new NotFoundException("Resource not found");

		assertNull(exception.getHeader());
		exception.setHeader(header);
		assertEquals(header, exception.getHeader());
	}
}
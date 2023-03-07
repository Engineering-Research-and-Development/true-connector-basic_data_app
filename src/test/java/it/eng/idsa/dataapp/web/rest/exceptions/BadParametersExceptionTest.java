package it.eng.idsa.dataapp.web.rest.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.util.UtilMessageService;

class BadParametersExceptionTest {

	private Message header;

	@BeforeEach
	public void init() {
		header = UtilMessageService.getArtifactRequestMessage();
	}

	@Test
	public void testConstructorWithHeader() {
		BadParametersException exception = new BadParametersException(header);

		assertEquals(header, exception.getHeader());
		assertNull(exception.getMessage());
		assertNull(exception.getCause());
	}

	@Test
	public void testConstructorWithMessage() {
		String message = "Invalid parameters";
		BadParametersException exception = new BadParametersException(message);

		assertEquals(message, exception.getMessage());
		assertNull(exception.getCause());
		assertNull(exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndHeader() {
		String message = "Invalid parameters";
		BadParametersException exception = new BadParametersException(message, header);

		assertEquals(message, exception.getMessage());
		assertNull(exception.getCause());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageAndCause() {
		String message = "Invalid parameters";
		Throwable cause = new RuntimeException("Cause");
		BadParametersException exception = new BadParametersException(message, cause);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertNull(exception.getHeader());
	}

	@Test
	public void testConstructorWithMessageCauseAndHeader() {
		String message = "Invalid parameters";
		Throwable cause = new RuntimeException("Cause");
		BadParametersException exception = new BadParametersException(message, cause, header);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertEquals(header, exception.getHeader());
	}

	@Test
	public void testSetAndGetHeader() {
		BadParametersException exception = new BadParametersException("Invalid parameters");

		assertNull(exception.getHeader());
		exception.setHeader(header);
		assertEquals(header, exception.getHeader());
	}
}

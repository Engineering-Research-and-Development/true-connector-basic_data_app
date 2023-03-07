package it.eng.idsa.dataapp.web.rest.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.util.UtilMessageService;

class VersionNotSupportedExceptionTest {

	private Message header;

	@BeforeEach
	public void init() {
		header = UtilMessageService.getArtifactRequestMessage();
	}

	@Test
	public void testConstructorWithHeader() {
		VersionNotSupportedException ex = new VersionNotSupportedException(header);

		assertEquals(header, ex.getHeader());
		assertNull(ex.getMessage());
		assertNull(ex.getCause());
	}

	@Test
	public void testConstructorWithMessage() {
		String message = "Version not supported";
		VersionNotSupportedException ex = new VersionNotSupportedException(message);

		assertEquals(message, ex.getMessage());
		assertNull(ex.getHeader());
		assertNull(ex.getCause());
	}

	@Test
	public void testConstructorWithMessageAndCause() {
		String message = "Version not supported";
		Throwable cause = new RuntimeException("Cause");
		VersionNotSupportedException ex = new VersionNotSupportedException(message, cause);

		assertEquals(message, ex.getMessage());
		assertNull(ex.getHeader());
		assertEquals(cause, ex.getCause());
	}

	@Test
	public void testConstructorWithMessageAndHeader() {
		String message = "Version not supported";
		VersionNotSupportedException ex = new VersionNotSupportedException(message, header);

		assertEquals(message, ex.getMessage());
		assertEquals(header, ex.getHeader());
		assertNull(ex.getCause());
	}

	@Test
	public void testConstructorWithMessageCauseAndHeader() {
		String message = "Version not supported";
		Throwable cause = new RuntimeException("Cause");
		VersionNotSupportedException ex = new VersionNotSupportedException(message, cause, header);

		assertEquals(message, ex.getMessage());
		assertEquals(header, ex.getHeader());
		assertEquals(cause, ex.getCause());
	}

	@Test
	public void testSetAndGetHeader() {
		VersionNotSupportedException exception = new VersionNotSupportedException("Version not supported");

		assertNull(exception.getHeader());
		exception.setHeader(header);
		assertEquals(header, exception.getHeader());
	}
}

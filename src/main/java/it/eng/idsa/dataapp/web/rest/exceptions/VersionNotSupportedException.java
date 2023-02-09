package it.eng.idsa.dataapp.web.rest.exceptions;

import de.fraunhofer.iais.eis.Message;

public class VersionNotSupportedException extends RuntimeException {

	private static final long serialVersionUID = 3385327887129320392L;
	private Message header;

	public VersionNotSupportedException(Message header) {
		super();
		this.setHeader(header);
	}

	public VersionNotSupportedException(String message) {
		super(message);
	}

	public VersionNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	public VersionNotSupportedException(String message, Message header) {
		super(message);
		this.header = header;
	}

	public VersionNotSupportedException(String message, Throwable cause, Message header) {
		super(message, cause);
		this.setHeader(header);
	}

	public Message getHeader() {
		return header;
	}

	public void setHeader(Message header) {
		this.header = header;
	}
}

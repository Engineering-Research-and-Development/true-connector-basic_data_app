package it.eng.idsa.dataapp.web.rest.exceptions;

import de.fraunhofer.iais.eis.Message;

public class TemporarilyNotAvailableException extends RuntimeException {

	private static final long serialVersionUID = -2712820209827697302L;

	private Message header;

	public TemporarilyNotAvailableException(Message header) {
		super();
		this.setHeader(header);
	}

	public TemporarilyNotAvailableException(String message) {
		super(message);
	}

	public TemporarilyNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public TemporarilyNotAvailableException(String message, Message header) {
		super(message);
		this.header = header;
	}

	public TemporarilyNotAvailableException(String message, Throwable cause, Message header) {
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

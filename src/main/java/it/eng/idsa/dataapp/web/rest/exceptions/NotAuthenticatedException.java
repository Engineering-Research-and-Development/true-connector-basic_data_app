package it.eng.idsa.dataapp.web.rest.exceptions;

import de.fraunhofer.iais.eis.Message;

public class NotAuthenticatedException extends RuntimeException {

	private static final long serialVersionUID = 6293544028797968814L;
	private Message header;

	public NotAuthenticatedException(Message header) {
		super();
		this.setHeader(header);
	}

	public NotAuthenticatedException(String message) {
		super(message);
	}

	public NotAuthenticatedException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotAuthenticatedException(String message, Message header) {
		super(message);
		this.header = header;
	}

	public NotAuthenticatedException(String message, Throwable cause, Message header) {
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

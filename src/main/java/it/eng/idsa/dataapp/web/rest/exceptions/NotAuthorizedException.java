package it.eng.idsa.dataapp.web.rest.exceptions;

import de.fraunhofer.iais.eis.Message;

public class NotAuthorizedException extends RuntimeException {

	private static final long serialVersionUID = -1884007894939156773L;
	private Message header;

	public NotAuthorizedException(Message header) {
		super();
		this.setHeader(header);
	}

	public NotAuthorizedException(String message) {
		super(message);
	}

	public NotAuthorizedException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotAuthorizedException(String message, Message header) {
		super(message);
		this.header = header;
	}

	public NotAuthorizedException(String message, Throwable cause, Message header) {
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

package it.eng.idsa.dataapp.web.rest.exceptions;

import de.fraunhofer.iais.eis.Message;

public class BadParametersException extends RuntimeException {

	private static final long serialVersionUID = -2453445386819241736L;
	private Message header;

	public BadParametersException(Message header) {
		super();
		this.setHeader(header);
	}

	public BadParametersException(String message) {
		super(message);
	}

	public BadParametersException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadParametersException(String message, Message header) {
		super(message);
		this.header = header;
	}

	public BadParametersException(String message, Throwable cause, Message header) {
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

package it.eng.idsa.dataapp.web.rest.exceptions;

import de.fraunhofer.iais.eis.Message;

public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = -2308527439846329115L;

	private Message header;

	public NotFoundException(Message header) {
		super();
		this.setHeader(header);
	}

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotFoundException(String message, Message header) {
		super(message);
		this.header = header;
	}

	public NotFoundException(String message, Throwable cause, Message header) {
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

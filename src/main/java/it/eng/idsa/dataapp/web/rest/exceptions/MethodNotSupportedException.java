package it.eng.idsa.dataapp.web.rest.exceptions;

import de.fraunhofer.iais.eis.Message;

public class MethodNotSupportedException extends RuntimeException {

	private static final long serialVersionUID = -4858677127818574039L;
	private Message header;

	public MethodNotSupportedException(Message header) {
		super();
		this.setHeader(header);
	}

	public MethodNotSupportedException(String message) {
		super(message);
	}

	public MethodNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	public MethodNotSupportedException(String message, Message header) {
		super(message);
		this.header = header;
	}

	public MethodNotSupportedException(String message, Throwable cause, Message header) {
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

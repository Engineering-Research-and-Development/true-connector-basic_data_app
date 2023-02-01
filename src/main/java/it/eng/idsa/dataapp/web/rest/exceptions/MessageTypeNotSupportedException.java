package it.eng.idsa.dataapp.web.rest.exceptions;

import de.fraunhofer.iais.eis.Message;

public class MessageTypeNotSupportedException extends RuntimeException {

	private static final long serialVersionUID = 355198703680276894L;
	private Message header;

	public MessageTypeNotSupportedException(Message header) {
		super();
		this.setHeader(header);
	}

	public MessageTypeNotSupportedException(String message) {
		super(message);
	}

	public MessageTypeNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	public MessageTypeNotSupportedException(String message, Message header) {
		super(message);
		this.header = header;
	}

	public MessageTypeNotSupportedException(String message, Throwable cause, Message header) {
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

package it.eng.idsa.dataapp.web.rest.exceptions;

import de.fraunhofer.iais.eis.Message;

public class MalformedMessageException extends RuntimeException {

	private static final long serialVersionUID = 4180395615968447927L;
	private Message header;

	public MalformedMessageException(Message header) {
		super();
		this.setHeader(header);
	}

	public MalformedMessageException(String message) {
		super(message);
	}

	public MalformedMessageException(String message, Throwable cause) {
		super(message, cause);
	}

	public MalformedMessageException(String message, Message header) {
		super(message);
		this.header = header;
	}

	public MalformedMessageException(String message, Throwable cause, Message header) {
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

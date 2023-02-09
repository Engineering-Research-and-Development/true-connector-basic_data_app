package it.eng.idsa.dataapp.web.rest.exceptions;

import de.fraunhofer.iais.eis.Message;

public class InternalRecipientException extends RuntimeException {

	private static final long serialVersionUID = -3764087406348269338L;
	private Message header;

	public InternalRecipientException(Message header) {
		super();
		this.setHeader(header);
	}

	public InternalRecipientException(String message) {
		super(message);
	}

	public InternalRecipientException(String message, Throwable cause) {
		super(message, cause);
	}

	public InternalRecipientException(String message, Message header) {
		super(message);
		this.header = header;
	}

	public InternalRecipientException(String message, Throwable cause, Message header) {
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

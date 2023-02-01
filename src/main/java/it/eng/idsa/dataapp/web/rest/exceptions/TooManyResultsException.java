package it.eng.idsa.dataapp.web.rest.exceptions;

import de.fraunhofer.iais.eis.Message;

public class TooManyResultsException extends RuntimeException {

	private static final long serialVersionUID = -7483894013643815437L;
	private Message header;

	public TooManyResultsException(Message header) {
		super();
		this.setHeader(header);
	}

	public TooManyResultsException(String message) {
		super(message);
	}

	public TooManyResultsException(String message, Throwable cause) {
		super(message, cause);
	}

	public TooManyResultsException(String message, Message header) {
		super(message);
		this.header = header;
	}

	public TooManyResultsException(String message, Throwable cause, Message header) {
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

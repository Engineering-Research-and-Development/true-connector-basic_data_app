package it.eng.idsa.dataapp.web.rest.exceptions;

public class CertificateMissingException extends RuntimeException {

	private static final long serialVersionUID = 4491200119186623932L;

	public CertificateMissingException(String message) {
		super(message);
	}

}

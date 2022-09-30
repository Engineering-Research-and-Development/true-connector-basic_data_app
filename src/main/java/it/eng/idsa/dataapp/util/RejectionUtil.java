package it.eng.idsa.dataapp.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import de.fraunhofer.iais.eis.RejectionReason;

public class RejectionUtil {

	public static ResponseEntity<String> HANDLE_REJECTION(RejectionReason rejectionReason) {
		switch (rejectionReason) {
		case BAD_PARAMETERS:
			return new ResponseEntity<String> ("Rejection occured with reason: BAD_PARAMETERS", HttpStatus.INTERNAL_SERVER_ERROR);

		case INTERNAL_RECIPIENT_ERROR:
			
			return new ResponseEntity<String> ("Rejection occured with reason: INTERNAL_RECIPIENT_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);

		case MALFORMED_MESSAGE:
			
			return new ResponseEntity<String> ("Rejection occured with reason: MALFORMED_MESSAGE", HttpStatus.BAD_REQUEST);

		case MESSAGE_TYPE_NOT_SUPPORTED:
			
			return new ResponseEntity<String> ("Rejection occured with reason: MESSAGE_TYPE_NOT_SUPPORTED", HttpStatus.BAD_REQUEST);

		case METHOD_NOT_SUPPORTED:
			
			return new ResponseEntity<String> ("Rejection occured with reason: METHOD_NOT_SUPPORTED", HttpStatus.METHOD_NOT_ALLOWED);

		case NOT_AUTHENTICATED:
			
			return new ResponseEntity<String> ("Rejection occured with reason: NOT_AUTHENTICATED", HttpStatus.NETWORK_AUTHENTICATION_REQUIRED);

		case NOT_AUTHORIZED:
			
			return new ResponseEntity<String> ("Rejection occured with reason: NOT_AUTHORIZED", HttpStatus.UNAUTHORIZED);

		case NOT_FOUND:
			
			return new ResponseEntity<String> ("Rejection occured with reason: NOT_FOUND", HttpStatus.NOT_FOUND);

		case TEMPORARILY_NOT_AVAILABLE:
			
			return new ResponseEntity<String> ("Rejection occured with reason: TEMPORARILY_NOT_AVAILABLE", HttpStatus.SERVICE_UNAVAILABLE);

		case TOO_MANY_RESULTS:
			
			return new ResponseEntity<String> ("Rejection occured with reason: TOO_MANY_RESULTS", HttpStatus.PAYLOAD_TOO_LARGE);

		case VERSION_NOT_SUPPORTED:
			
			return new ResponseEntity<String> ("Rejection occured with reason: VERSION_NOT_SUPPORTED", HttpStatus.BAD_REQUEST);

		default:
			return new ResponseEntity<String> ("Error while processing message", HttpStatus.BAD_REQUEST);
		}
	}

}

package it.eng.idsa.dataapp.service;

import org.apache.http.HttpEntity;

import de.fraunhofer.iais.eis.Message;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * Service Interface for managing MultiPartMessage.
 */
public interface MultiPartMessageService {
	String getHeader(String body);
	String getPayload(String body);

	Message getMessage(String body);
	Message getMessage(Object header);
	String addToken(Message message, String token);
	HttpEntity createMultipartMessage(String header, String payload/*, String boundary, String contentType*/);
	String getToken(String message);
	String removeToken(Message message);
	String getResponseHeader(Message header);
	String getResponseHeader(String header);
	Message createRejectionMessageLocalIssues(Message header);

}

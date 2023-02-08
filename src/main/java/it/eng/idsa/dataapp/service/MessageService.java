package it.eng.idsa.dataapp.service;

import java.util.List;

import it.eng.idsa.dataapp.domain.MessageIDS;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public interface MessageService {

	List<MessageIDS> getMessages();

	void setMessage(String contentType, String header, String payload);

}

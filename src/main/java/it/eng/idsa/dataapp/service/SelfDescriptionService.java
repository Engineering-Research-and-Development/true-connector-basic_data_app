package it.eng.idsa.dataapp.service;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.Message;

public interface SelfDescriptionService {

	/**
	 * Get connector self-description
	 * 
	 * @param message - used for logging potential error
	 * @return connector
	 * 
	 */
	Connector getSelfDescription(Message message);

	/**
	 * Get connector self-description as string
	 * 
	 * @param message - used for logging potential error
	 * @return connector
	 * 
	 */
	String getSelfDescriptionAsString(Message message);

	/**
	 * Checks if requested element exist in self-description
	 * 
	 * @param requestedElement - IDS request message
	 * @param connector - self-description
	 * @return exists
	 * 
	 */
	boolean artifactRequestedElementExist(ArtifactRequestMessage requestedElement, Connector connector);

	/**
	 * Checks if requested element exist in self description
	 * 
	 * @param message - IDS request message
	 * @param connector - self-description
	 * @return requestedElement
	 * 
	 */
	String getRequestedElement(DescriptionRequestMessage message, Connector connector);

}

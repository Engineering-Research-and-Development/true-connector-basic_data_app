package it.eng.idsa.dataapp.service;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.Message;

public interface SelfDescriptionService {

	/**
	 * Get connector self-description
	 * 
	 * @param Message message - used for logging potential error
	 * @return Connector connector
	 * 
	 */
	Connector getSelfDescription(Message message);

	/**
	 * Get connector self-description as string
	 * 
	 * @param Message message - used for logging potential error
	 * @return String connector
	 * 
	 */
	String getSelfDescriptionAsString(Message message);

	/**
	 * Checks if requested element exist in self-description
	 * 
	 * @param ArtifactRequestMessage requestedElement - IDS request message
	 * @param Connector              connector - self-description
	 * @return boolean exists
	 * 
	 */
	boolean artifactRequestedElementExist(ArtifactRequestMessage requestedElement, Connector connector);

	/**
	 * Checks if requested element exist in self description
	 * 
	 * @param DescriptionRequestMessage message - IDS request message
	 * @param Connector                 connector - self-description
	 * @return String requestedElement
	 * 
	 */
	String getRequestedElement(DescriptionRequestMessage message, Connector connector);

}

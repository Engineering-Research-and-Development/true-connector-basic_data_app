package it.eng.idsa.dataapp.service;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.Message;

public interface SelfDescriptionService {

	Connector getSelfDescription(Message message);

	String getSelfDescriptionAsString(Message message);

	boolean artifactRequestedElementExist(ArtifactRequestMessage requestedElement, Connector connector);

	String getRequestedElement(DescriptionRequestMessage message, Connector connector);

}

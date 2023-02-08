package it.eng.idsa.dataapp.service;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Message;

public interface SelfDescriptionService {

	Connector getSelfDescription(Message message);

	String getSelfDescriptionAsString(Message message);
}

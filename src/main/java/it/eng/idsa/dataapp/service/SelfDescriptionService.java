package it.eng.idsa.dataapp.service;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Message;

public interface SelfDescriptionService {

	public Connector getSelfDescription(Message message);

	public String getSelfDescriptionAsString(Message message);
}

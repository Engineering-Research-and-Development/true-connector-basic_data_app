package it.eng.idsa.dataapp.handler;

import java.util.Map;

import de.fraunhofer.iais.eis.Message;

public abstract class DataAppMessageHandler<M extends Message> {

	public abstract Map<String, Object> handleMessage(M message, Object payload);
}

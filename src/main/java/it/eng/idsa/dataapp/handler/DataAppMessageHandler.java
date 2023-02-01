package it.eng.idsa.dataapp.handler;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import de.fraunhofer.iais.eis.Message;

public abstract class DataAppMessageHandler {

	public static final String HEADER = "header";
	public static final String PAYLOAD = "payload";

	@Value("${application.ecc.issuer.connector}")
	private String issuerConnector;

	public abstract Map<String, Object> handleMessage(Message message, Object payload);

	protected URI whoIAm() {
		return URI.create("http://auto-generated");
	}

	protected URI whoIAmEngRDProvider() {
		return URI.create(issuerConnector);
	}
}

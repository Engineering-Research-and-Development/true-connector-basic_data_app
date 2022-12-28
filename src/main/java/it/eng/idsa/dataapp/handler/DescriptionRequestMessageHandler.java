package it.eng.idsa.dataapp.handler;

import java.util.Map;

import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.DescriptionRequestMessage;

@Component
public class DescriptionMessageHandler extends DataAppMessageHandler<DescriptionRequestMessage> {

	@Override
	public Map<String, Object> handleMessage(DescriptionRequestMessage message, Object payload) {
		System.out.println("DescriptionRequestMessage");
		return null;
	}

}

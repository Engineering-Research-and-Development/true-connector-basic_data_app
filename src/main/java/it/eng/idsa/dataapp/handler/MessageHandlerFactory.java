package it.eng.idsa.dataapp.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;

@Component
public class MessageHandlerFactory {

	@Autowired
	private ApplicationContext context;

	public DataAppMessageHandler createMessageHandler(Class<? extends Message> clazz) {
		DataAppMessageHandler handler = null;
		switch (clazz.getSimpleName().replace("Impl", "")) {
		case "ArtifactRequestMessage":
			handler = context.getBean(ArtifactMessageHandler.class);
			break;
		case "DescriptionRequestMessage":
			handler = context.getBean(DescriptionRequestMessageHandler.class);
			break;
		case "ContractRequestMessage":
			handler = context.getBean(ContractRequestMessageHandler.class);
			break;
		case "ContractAgreementMessage":
			handler = context.getBean(ContractAgreementMessageHandler.class);
			break;
		default:
			break;
		}
		return handler;
	}
}

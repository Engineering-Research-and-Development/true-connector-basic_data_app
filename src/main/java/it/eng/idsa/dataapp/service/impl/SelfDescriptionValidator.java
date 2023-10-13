package it.eng.idsa.dataapp.service.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.handler.DescriptionRequestMessageHandler;

@Component
public class SelfDescriptionValidator {

	private static final Logger logger = LoggerFactory.getLogger(DescriptionRequestMessageHandler.class);
	
	private boolean validateSelfDescription;
	
	public SelfDescriptionValidator(@Value("${application.validateSelfDescription:false}") boolean validateSelfDescription) {
		this.validateSelfDescription = validateSelfDescription;
	}

	/**
	 Validate if following fields are present in Self Description document:<br>
	 	cryptographic hash of Connector certificate,<br>
		Connector operator<br>
		data endpoints offered by Connector - defaultEndpoint gets validated by model constraint<br>
		log format of data endpoints offered<br>
		security profile of Connector (i.e. security features supported) - gets validated by model constraint<br>
		Connector ID - gets validated by model constraint
	 * @param payload - string representation of self description document
	 * @return result of the validation
	 */
	public boolean validateSelfDescription(String payload) {
		if(!validateSelfDescription) {
			logger.info("Validate self description disabled - skipping!");
			return true;
		}
		boolean selfDescriptionValid = false;
		logger.info("Checking DescriptionResponseMessage - validating received Self Description document");
		Serializer serializer = new Serializer();
		try {
			BaseConnector connector = serializer.deserialize(payload, BaseConnector.class);
			
			if(connector.getPublicKey() != null && connector.getPublicKey().getKeyValue() != null) {
				logger.debug("Public key present and key value is not null");
				selfDescriptionValid = true;
			} else {
				logger.warn("Public key not present or keyValue is null");
			}			 
			
		} catch (IOException e) {
			logger.warn("Could not deserialise payload to Connector class");
		}
		logger.info("SelfDescription valid - {}", selfDescriptionValid);
		return selfDescriptionValid;
	}

}

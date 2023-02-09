package it.eng.idsa.dataapp.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.service.SelfDescriptionService;
import it.eng.idsa.dataapp.web.rest.exceptions.InternalRecipientException;
import it.eng.idsa.dataapp.web.rest.exceptions.TemporarilyNotAvailableException;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Service
public class SelfDescriptionServiceImpl implements SelfDescriptionService {

	private static final Logger logger = LoggerFactory.getLogger(SelfDescriptionServiceImpl.class);

	private static Serializer serializer;
	static {
		serializer = new Serializer();
	}
	private RestTemplate restTemplate;
	private ECCProperties eccProperties;

	public SelfDescriptionServiceImpl(RestTemplateBuilder restTemplateBuilder, ECCProperties eccProperties) {
		this.restTemplate = restTemplateBuilder.build();
		this.eccProperties = eccProperties;
	}

	@Override
	public Connector getSelfDescription(Message message) {
		URI eccURI = null;

		try {
			eccURI = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), eccProperties.getPort(),
					eccProperties.getSelfdescriptionContext(), null, null);
			logger.info("Fetching self description from ECC {}.", eccURI.toString());

			ResponseEntity<String> response = restTemplate.exchange(eccURI, HttpMethod.GET, null, String.class);
			if (response != null) {
				if (response.getStatusCodeValue() == 200) {
					String selfDescription = response.getBody();
					logger.info("Deserializing self description.");
					logger.debug("Self description content: {}{}", System.lineSeparator(), selfDescription);

					return serializer.deserialize(selfDescription, Connector.class);
				} else {
					logger.error("Could not fetch self description, ECC responded with status {} and message \r{}",
							response.getStatusCodeValue(), response.getBody());

					throw new InternalRecipientException("Could not fetch self description", message);
				}
			}
			logger.error("Could not fetch self description, ECC did not respond");

			throw new TemporarilyNotAvailableException("Could not fetch self description, ECC did not respond",
					message);
		} catch (URISyntaxException e) {
			logger.error("Could not create URI for Self Description request.", e);

			throw new InternalRecipientException("Could not create URI for Self Description request", message);
		} catch (IOException e) {
			logger.error("Could not deserialize self description to Connector instance", e);

			throw new InternalRecipientException("Could not deserialize self description to Connector instance",
					message);
		}
	}

	@Override
	public String getSelfDescriptionAsString(Message message) {
		try {
			return MultipartMessageProcessor.serializeToJsonLD(getSelfDescription(message));
		} catch (IOException e) {
			logger.error("Could not serialize self description", e);

			throw new InternalRecipientException("Could not serialize self description", message);
		}
	}
}

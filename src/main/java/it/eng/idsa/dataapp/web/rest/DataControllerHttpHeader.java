package it.eng.idsa.dataapp.web.rest;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import it.eng.idsa.dataapp.util.MessageUtil;

@Controller
@ConditionalOnProperty(name = "application.dataapp.http.config", havingValue = "http-header")
@RequestMapping("/data")
public class DataControllerHttpHeader {

	private static final Logger logger = LogManager.getLogger(DataControllerHttpHeader.class);

	@PostMapping
	@Async
	public ResponseEntity<?> routerHttpHeader(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody(required = false) String payload)
			throws org.json.simple.parser.ParseException, ParseException, IOException {

		logger.info("Http Header request");

		httpHeaders.remove("Content-Length");
		httpHeaders.remove("Content-Type");

		logger.info("headers=" + httpHeaders);
		if (payload != null) {
			logger.info("payload lenght = " + payload.length());
		} else {
			logger.info("Payload is empty");
		}

		String responsePayload = MessageUtil.createResponsePayload();
		return ResponseEntity.ok().header("foo", "bar").headers(createHttpHeaderResponseHeaders())
				.header("Content-Type", MediaType.APPLICATION_JSON_VALUE).body(responsePayload);

	}
	
	
	private HttpHeaders createHttpHeaderResponseHeaders() {
		HttpHeaders headers = new HttpHeaders();

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date date = new Date();
		String formattedDate = dateFormat.format(date);

		headers.add("IDS-Messagetype", "ids:ArtifactResponseMessage");
		headers.add("IDS-Id", "https://w3id.org/idsa/autogen/artifactResponseMessage/" + UUID.randomUUID().toString());
		headers.add("IDS-Issued", formattedDate);
		headers.add("IDS-ModelVersion", "4.0.0");
		headers.add("IDS-IssuerConnector", "http://w3id.org/engrd/connector");

		return headers;
	}
}

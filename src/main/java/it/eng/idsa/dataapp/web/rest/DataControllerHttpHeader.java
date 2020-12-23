package it.eng.idsa.dataapp.web.rest;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
@ConditionalOnProperty(name = "application.http.config", havingValue = "http-header")
@RequestMapping("/data")
public class DataControllerHttpHeader {

	private static final Logger logger = LogManager.getLogger(DataControllerBodyBinary.class);

	@PostMapping
	@Async
	public ResponseEntity<?> routerHttpHeader(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody(required = false) String payload)
			throws org.json.simple.parser.ParseException, ParseException, IOException {

		logger.info("Http Header request");
//				Map<String, String> headerAsMap = new HashMap<String, String>();
//				headerAsMap.put("@type", httpHeaders.get("IDS-Messagetype").get(0));
//				headerAsMap.put("@id", httpHeaders.get("IDS-Id").get(0));
//				headerAsMap.put("issued", httpHeaders.get("IDS-Issued").get(0));
//				headerAsMap.put("modelVersion", httpHeaders.get("IDS-ModelVersion").get(0));
//				headerAsMap.put("issuerConnector", httpHeaders.get("IDS-IssuerConnector").get(0));
//				headerAsMap.put("transferContract", httpHeaders.get("IDS-TransferContract").get(0));
//				headerAsMap.put("correlationMessage", httpHeaders.get("IDS-CorrelationMessage").get(0));

//				String header = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(headerAsMap);

		httpHeaders.remove("Content-Length");
		httpHeaders.remove("Content-Type");

		logger.info("headers=" + httpHeaders);
		if (payload != null) {
			logger.info("payload lenght = " + payload.length());
		} else {
			logger.info("Payload is empty");
		}

		String responsePayload = createResponsePayload();
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
		headers.add("IDS-IssuerConnector", "http://iais.fraunhofer.de/ids/mdm-connector");
//				headers.add("IDS-TransferContract", "https://mdm-connector.ids.isst.fraunhofer.de/examplecontract/bab-bayern-sample/");
//				headers.add("IDS-CorrelationMessage", "http://industrialdataspace.org/connectorUnavailableMessage/1a421b8c-3407-44a8-aeb9-253f145c869a");

		return headers;
	}

	private String createResponsePayload() {
		// Put check sum in the payload
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String formattedDate = dateFormat.format(date);

		Map<String, String> jsonObject = new HashMap<>();
		jsonObject.put("firstName", "John");
		jsonObject.put("lastName", "Doe");
		jsonObject.put("dateOfBirth", formattedDate);
		jsonObject.put("address", "591  Franklin Street, Pennsylvania");
		jsonObject.put("checksum", "ABC123 " + formattedDate);
		Gson gson = new GsonBuilder().create();
		return gson.toJson(jsonObject);

	}

}

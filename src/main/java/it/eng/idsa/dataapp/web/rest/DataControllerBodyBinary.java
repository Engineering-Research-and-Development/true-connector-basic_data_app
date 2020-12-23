package it.eng.idsa.dataapp.web.rest;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.service.impl.MultiPartMessageServiceImpl;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Controller
@ConditionalOnProperty(name = "application.http.config", havingValue = "mixed")
@RequestMapping("/data")
public class DataControllerBodyBinary {

	private static final Logger logger = LogManager.getLogger(DataControllerBodyBinary.class);

	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;

	@PostMapping
	@Async
	public ResponseEntity<?> routerBinary(@RequestHeader HttpHeaders httpHeaders,
			@RequestPart(value = "header") Message headerMessage,
			@RequestHeader(value = "Response-Type", required = false) String responseType,
			@RequestPart(value = "payload", required = false) String payload)
			throws org.json.simple.parser.ParseException, ParseException, IOException {

		logger.info("Multipart/mixed request");

		// Convert de.fraunhofer.iais.eis.Message to the String
		String headerSerialized = new Serializer().serializePlainJson(headerMessage);
		logger.info("header=" + headerSerialized);
		logger.info("headers=" + httpHeaders);
		if (payload != null) {
			logger.info("payload lenght = " + payload.length());
		} else {
			logger.info("Payload is empty");
		}

		String headerResponse = multiPartMessageServiceImpl.getResponseHeader(headerMessage);
		String responsePayload = createResponsePayload();
		MultipartMessage responseMessage = new MultipartMessageBuilder().withHeaderContent(headerResponse)
				.withPayloadContent(responsePayload).build();
		String responseMessageString = MultipartMessageProcessor.multipartMessagetoString(responseMessage, false);

		return ResponseEntity.ok().header("foo", "bar")
				.header("Content-Type", "multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6;charset=UTF-8")
				.body(responseMessageString);

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

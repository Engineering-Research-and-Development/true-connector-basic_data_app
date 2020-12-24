package it.eng.idsa.dataapp.web.rest;

import java.io.IOException;

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

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.dataapp.util.PayloadUtil;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Controller
@ConditionalOnProperty(name = "application.http.config", havingValue = "mixed")
@RequestMapping("/data")
public class DataControllerBodyBinary {

	private static final Logger logger = LogManager.getLogger(DataControllerBodyBinary.class);

	@Autowired
	private MultiPartMessageService multiPartMessageService;

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

		String headerResponse = multiPartMessageService.getResponseHeader(headerMessage);
		String responsePayload = PayloadUtil.createResponsePayload();
		MultipartMessage responseMessage = new MultipartMessageBuilder().withHeaderContent(headerResponse)
				.withPayloadContent(responsePayload).build();
		String responseMessageString = MultipartMessageProcessor.multipartMessagetoString(responseMessage, false);

		return ResponseEntity.ok().header("foo", "bar")
				.header("Content-Type", "multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6;charset=UTF-8")
				.body(responseMessageString);

	}
}

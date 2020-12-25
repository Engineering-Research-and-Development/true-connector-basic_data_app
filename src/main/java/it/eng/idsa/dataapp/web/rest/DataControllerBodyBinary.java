package it.eng.idsa.dataapp.web.rest;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.http.ParseException;
import org.apache.http.entity.mime.MIME;
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
import it.eng.idsa.dataapp.util.MessageUtil;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Controller
@ConditionalOnProperty(name = "application.dataapp.http.config", havingValue = "mixed")
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
			logger.info("payload length = " + payload.length());
		} else {
			logger.info("Payload is empty");
		}

		String headerResponse = multiPartMessageService.getResponseHeader(headerMessage);
		String responsePayload = MessageUtil.createResponsePayload();
		MultipartMessage responseMessage = new MultipartMessageBuilder().withHeaderContent(headerResponse)
				.withPayloadContent(responsePayload).build();
		String responseMessageString = MultipartMessageProcessor.multipartMessagetoString(responseMessage, false);
		
		Optional<String> boundary = getMessageBoundaryFromMessage(responseMessageString);
		String contentType = "multipart/mixed; boundary=" + boundary.orElse("---aaa") + ";charset=UTF-8";

		return ResponseEntity.ok()
				.header("foo", "bar")
				.header(MIME.CONTENT_TYPE, contentType)
				.body(responseMessageString);

	}
	
	private Optional<String> getMessageBoundaryFromMessage(String message) {
        String boundary = null;
        Stream<String> lines = message.lines();
        boundary = lines.filter(line -> line.startsWith("--"))
                .findFirst()
                .get();
        return Optional.ofNullable(boundary);
    }
}

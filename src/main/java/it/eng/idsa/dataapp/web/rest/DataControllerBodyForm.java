package it.eng.idsa.dataapp.web.rest;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.dataapp.util.MessageUtil;

@RestController
@ConditionalOnProperty(name = "application.dataapp.http.config", havingValue = "form")
@RequestMapping({"/data"})
public class DataControllerBodyForm {
	private static final Logger logger = LogManager.getLogger(DataControllerBodyForm.class);

	@Autowired
	private MultiPartMessageService multiPartMessageService;

	
	@PostMapping
    @Async
    public ResponseEntity<?> routerForm(@RequestHeader HttpHeaders httpHeaders,
    									@RequestParam(value = "header") String header,
                                        @RequestHeader(value = "Response-Type", required = false) String responseType,
                                        @RequestParam(value = "payload", required = false) String payload)
                                        throws ParseException, IOException {
		
		logger.info("Multipart/form request");

		// Received "header" and "payload"
		logger.info("header"+header);
		logger.info("headers=" + httpHeaders);
		if (payload != null) {
			logger.info("payload lenght = " + payload.length());
		} else {
			logger.info("Payload is empty");
		}
		
		// prepare body response - multipart message.
		
		HttpEntity resultEntity = multiPartMessageService.createMultipartMessageForm(multiPartMessageService.getResponseHeader(header), 
				MessageUtil.createResponsePayload(), null, ContentType.APPLICATION_JSON);
		
		return ResponseEntity.ok()
				.header("foo", "bar")
				.header(resultEntity.getContentType().getName(), resultEntity.getContentType().getValue())
				.body(resultEntity.getContent().readAllBytes());
		
	}
	
	
}

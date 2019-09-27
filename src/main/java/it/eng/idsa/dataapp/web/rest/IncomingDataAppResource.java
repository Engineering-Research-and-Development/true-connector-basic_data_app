package it.eng.idsa.dataapp.web.rest;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.eng.idsa.dataapp.domain.MessageIDS;
import it.eng.idsa.dataapp.service.impl.MessageServiceImpl;
import it.eng.idsa.dataapp.service.impl.MultiPartMessageServiceImpl;


/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * REST controller for managing IncomingDataAppResource.
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping({ "/incoming-data-app" })
public class IncomingDataAppResource {
	
	private static final Logger logger = LogManager.getLogger(IncomingDataAppResource.class);
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Autowired
	private MessageServiceImpl messageServiceImpl;
	
	/*
	@PostMapping(value="/dataAppIncomingMessage", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, "multipart/mixed", MediaType.ALL_VALUE }, produces= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> receiveMessage(@RequestHeader (value="Content-Type", required=false) String contentType,  @RequestParam("header")  Object header,             
            @RequestParam("payload") Object payload   ) {
		logger.debug("POST /dataAppIncomingMessage");
		messageServiceImpl.setMessage(contentType, header.toString(), payload.toString());
		return ResponseEntity.ok().build();
	}
	*/
	
	
	
	
	@PostMapping("/dataAppIncomingMessage")
	public ResponseEntity<?> postMessage(@RequestBody String data){
		logger.info("Enter to the end-point: dataAppIncomingMessage");

		String header=multiPartMessageServiceImpl.getHeader(data);
		String payload=multiPartMessageServiceImpl.getPayload(data);
		messageServiceImpl.setMessage("", header.toString(), payload.toString());

		logger.info("message="+data);
		return ResponseEntity.ok().build();
	}
		
	@GetMapping("/dataAppIncomingMessage")
	public List<MessageIDS> testReceiveMessage() {
		logger.debug("GET /dataAppIncomingMessage");
		return messageServiceImpl.getMessages();
	}
	
}

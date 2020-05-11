package it.eng.idsa.dataapp.web.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;


import de.fraunhofer.iais.eis.Message;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.domain.MessageIDS;
import it.eng.idsa.dataapp.service.impl.MessageServiceImpl;
import it.eng.idsa.dataapp.service.impl.MultiPartMessageServiceImpl;
import it.eng.idsa.dataapp.service.impl.RecreateFileServiceImpl;
import nl.tno.ids.common.multipart.MultiPartMessage;


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
@ConditionalOnProperty(
		value="application.websocket.isEnabled",
		havingValue = "false",
		matchIfMissing = true)
@RequestMapping({ "/incoming-data-app" })
public class IncomingDataAppResource {

	private static final Logger logger = LogManager.getLogger(IncomingDataAppResource.class);

	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;

	@Autowired
	private MessageServiceImpl messageServiceImpl;
	
	@Autowired
	private RecreateFileServiceImpl recreateFileServiceImpl;

	/*
	@PostMapping(value="/dataAppIncomingMessage", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, "multipart/mixed", MediaType.ALL_VALUE }, produces= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> receiveMessage(@RequestHeader (value="Content-Type", required=false) String contentType,  @RequestParam("header")  Object header,             
            @RequestParam("payload") Object payload   ) {
		logger.debug("POST /dataAppIncomingMessage");
		messageServiceImpl.setMessage(contentType, header.toString(), payload.toString());
		return ResponseEntity.ok().build();
	}
	 */




	@PostMapping("/dataAppIncomingMessageReceiver")
	public ResponseEntity<?> postMessageReceiver(@RequestBody String data){
		logger.info("Enter to the end-point: dataAppIncomingMessage Receiver side");
		String header=multiPartMessageServiceImpl.getHeader(data);
		String payload=multiPartMessageServiceImpl.getPayload(data);
		messageServiceImpl.setMessage("", header.toString(), payload.toString());
		logger.info("message="+data);
		return ResponseEntity.ok().build();
	}



	@PostMapping(value="/postMultipartMessage", produces= /*MediaType.MULTIPART_FORM_DATA_VALUE*/ MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> postMessage(@RequestHeader("Content-Type") String contentType,
			@RequestHeader("Forward-To") String forwardTo,  @RequestParam(value = "header",required = false)  Object header,             
			@RequestParam(value = "payload", required = false) Object payload   ) {
		logger.info("header"+header);
		logger.info("payload="+payload);
		logger.info("forwardTo="+forwardTo);
		return new ResponseEntity<String>("postMultipartMessage endpoint: success\n", HttpStatus.OK);
	}
	
	//======================================================================
	// body: binary
	//======================================================================
	@RequestMapping(
            value = "/routerBodyBinary",
            method = RequestMethod.POST,
            produces = {MediaType.MULTIPART_FORM_DATA_VALUE, "multipart/mixed"}
    )
    @Async
    public ResponseEntity<?> routerBinary(@RequestPart(value = "header") Message headerMessage,
                                                    @RequestHeader(value = "Response-Type", required = false) String responseType,
                                                    @RequestPart(value = "payload", required = false) String payload) throws org.json.simple.parser.ParseException, ParseException, IOException {
		
		// Convert de.fraunhofer.iais.eis.Message to the String		
		String msgSerialized = new Serializer().serializePlainJson(headerMessage);
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
		String header=new Serializer().serializePlainJson(jsonObject);
		
		logger.info("header="+header);
		logger.info("payload lenght="+payload.length());
		
		// Recreate the file
		recreateFileServiceImpl.recreateTheFile(payload);
		logger.info("The file is recreated from the MultipartMessage");
		
		// Put check sum in the payload
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		payload="{\"checksum\":\"ABC123 " + dateFormat.format(date) + "\"}";
		
		// payload will be empty in the multipart message
//		payload = null;
		
		// prepare body response - multipart message.
		String responseString = new MultiPartMessage.Builder()
                										.setHeader(header)
                										.setPayload(payload)
                										.build()
                										.toString();
		
		return ResponseEntity.ok()
				.header("Content-Type", "multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6;charset=UTF-8")
				.body(responseString);
		
	}
	
	//======================================================================
	// body: form-data
	//======================================================================
	@RequestMapping(
            value = "/routerBodyFormData",
            method = RequestMethod.POST,
            produces = {MediaType.MULTIPART_FORM_DATA_VALUE, "multipart/mixed"}
    )
    @Async
    public ResponseEntity<?> routerMix(@RequestParam(value = "header") String header,
                                                    @RequestHeader(value = "Response-Type", required = false) String responseType,
                                                    @RequestParam(value = "payload", required = false) String payload) throws ParseException, IOException {
        // Received "header" and "payload"
		logger.info("header"+header);
		logger.info("payload lenght="+payload.length());
		
		// Recreate the file
		recreateFileServiceImpl.recreateTheFile(payload);
		logger.info("The file is recreated from the MultipartMessage");
		
		// Put check sum in the payload
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		payload="{\"checksum\":\"ABC123 " + dateFormat.format(date) + "\"}";
		
		// payload will be empty in the multipart message
//		payload = null;
		
		// prepare body response - multipart message.
		String responseString = new MultiPartMessage.Builder()
														.setHeader(header)
														.setPayload(payload)
														.build()
														.toString();
		
		return ResponseEntity.ok()
				.header("Content-Type", "multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6;charset=UTF-8")
				.body(responseString);
		
	}

	@PostMapping("/dataAppIncomingMessageSender")
	public ResponseEntity<?> postMessageSender(@RequestBody String data){
		logger.info("Enter to the end-point: dataAppIncomingMessage Sender side");

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

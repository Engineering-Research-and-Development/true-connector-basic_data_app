package it.eng.idsa.dataapp.web.rest;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.domain.MessageIDS;
import it.eng.idsa.dataapp.service.impl.MessageServiceImpl;
import it.eng.idsa.dataapp.service.impl.MultiPartMessageServiceImpl;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;


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
//		logger.info("payload="+payload);
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
    public ResponseEntity<?> routerBinary(@RequestHeader HttpHeaders httpHeaders,
    										@RequestPart(value = "header") Message headerMessage,
                                            @RequestHeader(value = "Response-Type", required = false) String responseType,
                                            @RequestPart(value = "payload", required = false) String payload) throws org.json.simple.parser.ParseException, ParseException, IOException {

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
		MultipartMessage responseMessage = new MultipartMessageBuilder()
                										.withHeaderContent(headerResponse)
                										.withPayloadContent(responsePayload)
                										.build();
        String responseMessageString = MultipartMessageProcessor.multipartMessagetoString(responseMessage, false);
		
		return ResponseEntity.ok()
				.header("foo", "bar")
				.header("Content-Type", "multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6;charset=UTF-8")
				.body(responseMessageString);
		
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
    public ResponseEntity<?> routerMix(@RequestHeader HttpHeaders httpHeaders,
    										@RequestParam(value = "header") String header,
                                            @RequestHeader(value = "Response-Type", required = false) String responseType,
                                            @RequestParam(value = "payload", required = false) String payload) throws ParseException, IOException {
		
		logger.info("Multipart/form request");

		// Received "header" and "payload"
		logger.info("header"+header);
		logger.info("headers=" + httpHeaders);
		if (payload != null) {
			logger.info("payload lenght = " + payload.length());
		} else {
			logger.info("Payload is empty");
		}
		
		String headerResponse = multiPartMessageServiceImpl.getResponseHeader(header);
		String responsePayload = createResponsePayload();

		// prepare body response - multipart message.
		MultipartMessage responseMessage = new MultipartMessageBuilder()
				.withHeaderContent(headerResponse)
				.withPayloadContent(responsePayload)
				.build();
		String responseMessageString = MultipartMessageProcessor.multipartMessagetoString(responseMessage, false);
		
		return ResponseEntity.ok()
				.header("foo", "bar")
				.header("Content-Type", "multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6;charset=UTF-8")
				.body(responseMessageString);
		
	}
	
		//======================================================================
		// http-header
		//======================================================================
		@RequestMapping(
	            value = "/routerHttpHeader",
	            method = RequestMethod.POST,
	            produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.TEXT_PLAIN_VALUE}
	    )
	    @Async
	    public ResponseEntity<?> routerHttpHeader(@RequestHeader HttpHeaders httpHeaders,
	                                                    @RequestBody(required = false) String payload) throws org.json.simple.parser.ParseException, ParseException, IOException {

			logger.info("Http Header request");
//			Map<String, String> headerAsMap = new HashMap<String, String>();
//			headerAsMap.put("@type", httpHeaders.get("IDS-Messagetype").get(0));
//			headerAsMap.put("@id", httpHeaders.get("IDS-Id").get(0));
//			headerAsMap.put("issued", httpHeaders.get("IDS-Issued").get(0));
//			headerAsMap.put("modelVersion", httpHeaders.get("IDS-ModelVersion").get(0));
//			headerAsMap.put("issuerConnector", httpHeaders.get("IDS-IssuerConnector").get(0));
//			headerAsMap.put("transferContract", httpHeaders.get("IDS-TransferContract").get(0));
//			headerAsMap.put("correlationMessage", httpHeaders.get("IDS-CorrelationMessage").get(0));
			
//			String header = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(headerAsMap);
			
			httpHeaders.remove("Content-Length");
			httpHeaders.remove("Content-Type");
			
			logger.info("headers=" + httpHeaders);
			if (payload != null) {
				logger.info("payload lenght = " + payload.length());
			} else {
				logger.info("Payload is empty");
			}
			
			String responsePayload = createResponsePayload();
			return ResponseEntity.ok()
					.header("foo", "bar")
					.headers(createHttpHeaderResponseHeaders())
					.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
					.body(responsePayload);
			
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
//			headers.add("IDS-TransferContract", "https://mdm-connector.ids.isst.fraunhofer.de/examplecontract/bab-bayern-sample/");
//			headers.add("IDS-CorrelationMessage", "http://industrialdataspace.org/connectorUnavailableMessage/1a421b8c-3407-44a8-aeb9-253f145c869a");
			
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

package it.eng.idsa.dataapp.web.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactRequestMessageBuilder;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ResponseMessage;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.service.RecreateFileService;
import it.eng.idsa.dataapp.service.impl.MultiPartMessageServiceImpl;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.streamer.WebSocketClientManager;
import it.eng.idsa.streamer.util.MultiPartMessageServiceUtil;
import it.eng.idsa.streamer.websocket.receiver.server.FileRecreatorBeanExecutor;

/**
 * @author Antonio Scatoloni
 */

@RestController
@EnableAutoConfiguration
@RequestMapping({ "/" })
public class FileSenderResource {
	private static final Logger logger = LogManager.getLogger(FileSenderResource.class);

	@Autowired
	MultiPartMessageServiceImpl multiPartMessageService;
	
	@Autowired
	RecreateFileService recreateFileService;
	
	@PostMapping("/requireandsavefile")
	@ResponseBody
	public String requireAndSaveFile(@RequestHeader("Forward-To-Internal") String forwardToInternal,
			@RequestHeader("Forward-To") String forwardTo, 
			@RequestBody String fileName) throws Exception {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples-multipart-messages/" + fileName);
		String message = IOUtils.toString(is, "UTF8");
		FileRecreatorBeanExecutor.getInstance().setForwardTo(forwardTo);
		String responseMessage = WebSocketClientManager.getMessageWebSocketSender()
				.sendMultipartMessageWebSocketOverHttps(message, forwardToInternal);

		String fileNameSaved = saveFileToDisk(responseMessage, message);

		String payload = "{​​\"message\":\"File '" + fileNameSaved + "' created successfully\"}";
		MultipartMessage multipartMessage = new MultipartMessage(
				new HashMap<>(), 
				new HashMap<>(),
				multiPartMessageService.getMessage(responseMessage),
				new HashMap<>(), 
				payload, 
				new HashMap<>(),
				null,
				null);
		return MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);

	}
	
	@PostMapping("/requirefile")
	@ResponseBody
	public String requireFile(@RequestHeader("Forward-To-Internal") String forwardToInternal,
			@RequestHeader("Forward-To") String forwardTo, 
			@RequestBody String fileName) throws Exception {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples-multipart-messages/" + fileName);
		String message = IOUtils.toString(is, "UTF8");
		FileRecreatorBeanExecutor.getInstance().setForwardTo(forwardTo);
		String responseMessage = WebSocketClientManager.getMessageWebSocketSender()
				.sendMultipartMessageWebSocketOverHttps(message, forwardToInternal);

		return responseMessage;

	}
	
	
	@PostMapping("/artifactRequestMessage")
	@ResponseBody
	public String requestArtifact(@RequestHeader("Forward-To-Internal") String forwardToInternal,
			@RequestHeader("Forward-To") String forwardTo, @RequestParam String requestedArtifact,
			@Nullable @RequestBody String payload) throws Exception {
		URI requestedArtifactURI = URI
				.create("http://w3id.org/engrd/connector/artifact/" + requestedArtifact);
		Message artifactRequestMessage = new ArtifactRequestMessageBuilder()
				._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
				._issuerConnector_(URI.create("http://w3id.org/engrd/connector"))
				._modelVersion_("4.0.0")
				._requestedArtifact_(requestedArtifactURI)
				.build();
		Serializer serializer = new Serializer();
		String requestMessage = serializer.serialize(artifactRequestMessage);
		FileRecreatorBeanExecutor.getInstance().setForwardTo(forwardTo);
		String responseMessage = WebSocketClientManager.getMessageWebSocketSender()
				.sendMultipartMessageWebSocketOverHttps(requestMessage, payload, forwardToInternal);

		String fileNameSaved = saveFileToDisk(responseMessage, artifactRequestMessage);

		String payloadResponse = null;
		if(fileNameSaved != null) {
			payloadResponse = "{​​\"message\":\"File '" + fileNameSaved + "' created successfully\"}";
		}
		MultipartMessage multipartMessage = new MultipartMessage(
				new HashMap<>(), 
				new HashMap<>(),
				multiPartMessageService.getMessage(responseMessage),
				new HashMap<>(), 
				payloadResponse, 
				new HashMap<>(),
				null,
				null);
		return MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);
	}
	
	private String saveFileToDisk(String responseMessage, String requestMessage) throws IOException {
		Message requestMsg = multiPartMessageService.getMessage(requestMessage);
		Message responseMsg = multiPartMessageService.getMessage(responseMessage);
		logger.info("Response message: {} ", MultipartMessageProcessor.serializeToJsonLD(responseMsg));
		String payload = MultiPartMessageServiceUtil.getPayload(responseMessage);
		logger.debug("Response payload: {} ", payload);

		String requestedArtifact = null;
		if (requestMsg instanceof ArtifactRequestMessage && responseMsg instanceof ResponseMessage) {
			requestedArtifact = ((ArtifactRequestMessage) requestMsg).getRequestedArtifact().getPath().split("/")[2];
			logger.info("About to save file " + requestedArtifact);
			recreateFileService.recreateTheFile(payload, new File(requestedArtifact));
			logger.info("File saved");
		} else {
			logger.info("Did not have ArtifactRequestMessage and ResponseMessage - nothing to save");
		}
		
		return requestedArtifact;
	}
	
	private String saveFileToDisk(String responseMessage, Message requestMessage) throws IOException {
		Message responseMsg = multiPartMessageService.getMessage(responseMessage);

		String requestedArtifact = null;
		if (requestMessage instanceof ArtifactRequestMessage && responseMsg instanceof ArtifactResponseMessage) {
			String payload = MultiPartMessageServiceUtil.getPayload(responseMessage);
			String reqArtifact = ((ArtifactRequestMessage) requestMessage).getRequestedArtifact().getPath();
			// get resource from URI http://w3id.org/engrd/connector/artifact/ + requestedArtifact
			requestedArtifact = reqArtifact.substring(reqArtifact.lastIndexOf('/') + 1);
			logger.info("About to save file " + requestedArtifact);
			recreateFileService.recreateTheFile(payload, new File(requestedArtifact));
			logger.info("File saved");
		} else {
			logger.info("Did not have ArtifactRequestMessage and ResponseMessage - nothing to save");
			requestedArtifact = null;
		}
		
		return requestedArtifact;
	}

}

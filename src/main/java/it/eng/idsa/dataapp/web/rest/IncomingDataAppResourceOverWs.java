package it.eng.idsa.dataapp.web.rest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.handler.DataAppMessageHandler;
import it.eng.idsa.dataapp.handler.MessageHandlerFactory;
import it.eng.idsa.dataapp.service.ThreadService;
import it.eng.idsa.dataapp.util.MessageUtil;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.streamer.WebSocketServerManager;

/**
 * @author Antonio Scatoloni
 */

public class IncomingDataAppResourceOverWs implements PropertyChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(IncomingDataAppResourceOverWs.class);

	@Value("${application.dataLakeDirectory}")
	private Path dataLakeDirectory;

	@Autowired
	private MessageHandlerFactory factory;

	@Autowired
	private MessageUtil messageUtil;

	@Autowired
	private ThreadService threadService;
	@Value("#{new Boolean('${application.extractPayloadFromResponse:false}')}")
	private Boolean extractPayloadFromResponse;

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		threadService.setThreadLocalValue("wss", true);
		String requestMessageMultipart = (String) evt.getNewValue();
		logger.debug("Received message over WSS");
		MultipartMessage receivedMessage = MultipartMessageProcessor.parseMultipartMessage(requestMessageMultipart);
		Message requestMessage = receivedMessage.getHeaderContent();
		String payload = receivedMessage.getPayloadContent();
		String response = null;

		DataAppMessageHandler handler = factory.createMessageHandler(requestMessage.getClass());
		try {
			Map<String, Object> responseMap = handler.handleMessage(requestMessage, payload);

			response = createWsResponse(responseMap);
			WebSocketServerManager.getMessageWebSocketResponse().sendResponse(response);
			threadService.removeThreadLocalValue("wss");

		} catch (Exception e) {
			// Refactor this in error handling
			logger.error(e.getMessage());

			Message rejectionMessage = messageUtil.createRejectionMessage(requestMessage);
			MultipartMessage responseMessageRejection = new MultipartMessageBuilder()
					.withHeaderContent(rejectionMessage).withPayloadContent(null).build();
			String responseMessageString = MultipartMessageProcessor.multipartMessagetoString(responseMessageRejection,
					false, Boolean.TRUE);

			WebSocketServerManager.getMessageWebSocketResponse().sendResponse(responseMessageString);
			threadService.removeThreadLocalValue("wss");
		}
	}

	private String createWsResponse(Map<String, Object> responseMap) {
		Object payload = responseMap.get(DataAppMessageHandler.PAYLOAD);
		String payloadString;
		if (payload instanceof JSONObject) {
			payloadString = ((JSONObject) payload).toJSONString();
		} else {
			payloadString = (String) payload;
		}
		MultipartMessage responseMessageMultipart = new MultipartMessageBuilder()
				.withHeaderContent((Message) responseMap.get(DataAppMessageHandler.HEADER))
				.withPayloadContent(payloadString).build();

		return MultipartMessageProcessor.multipartMessagetoString(responseMessageMultipart, false, Boolean.TRUE);
	}
}

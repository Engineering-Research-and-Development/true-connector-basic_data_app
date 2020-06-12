package it.eng.idsa.dataapp.web.rest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.streamer.WebSocketServerManager;

/**
 * @author Antonio Scatoloni
 */

public class IncomingDataAppResourceOverWs implements PropertyChangeListener {

    @Autowired
    private MultiPartMessageService multiPartMessageService;

    private String responseMessage;

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.setResponseMessage((String) evt.getNewValue());
        WebSocketServerManager.getMessageWebSocketResponse().sendResponse(createDummyResponse(getResponseMessage()));
    }


    //TODO
    private String createDummyResponse(String responseMessageInput) {
        String responseMessageString = null;
        try {
            String header = multiPartMessageService.getHeader(responseMessageInput);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            String payload="{\"checksum\":\"ABC123 " + dateFormat.format(date) + "\"}";
            // prepare multipart message.
            MultipartMessage responseMessage = new MultipartMessageBuilder()
    				.withHeaderContent(header)
    				.withPayloadContent(payload)
    				.build();
    		responseMessageString = MultipartMessageProcessor.multipartMessagetoString(responseMessage, false);
            
        } catch (Exception e) {
            e.printStackTrace();
            //TODO Rejection
        }
        return responseMessageString;
    }


}

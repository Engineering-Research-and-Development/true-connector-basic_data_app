package it.eng.idsa.dataapp.web.rest;

import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.streamer.WebSocketServerManager;
import nl.tno.ids.common.multipart.MultiPartMessage;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private String createDummyResponse(String responseMessage) {
        String responseString = null;
        try {
            String header = multiPartMessageService.getHeader(responseMessage);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            String payload="{\"checksum\":\"ABC123 " + dateFormat.format(date) + "\"}";
            // prepare multipart message.
            responseString = new MultiPartMessage.Builder()
                    .setHeader(header)
                    .setPayload(payload)
                    .build()
                    .toString();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO Rejection
        }
        return responseString;
    }


}

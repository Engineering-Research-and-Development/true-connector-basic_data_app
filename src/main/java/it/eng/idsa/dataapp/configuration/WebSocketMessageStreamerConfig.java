package it.eng.idsa.dataapp.configuration;

import it.eng.idsa.dataapp.web.rest.IncomingDataAppResourceOverWs;
import it.eng.idsa.streamer.WebSocketServerManager;
import it.eng.idsa.streamer.websocket.receiver.server.FileRecreatorBeanExecutor;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Antonio Scatoloni
 */

@Configuration
@ConditionalOnProperty(
        value="application.websocket.isEnabled",
        havingValue = "true",
        matchIfMissing = false)
public class WebSocketMessageStreamerConfig {
    @Value("${server.port}")
    private int port;

    @Bean
    public FileRecreatorBeanExecutor fileRecreatorBeanExecutor() throws SchedulerException {
        FileRecreatorBeanExecutor fileRecreatorBeanExecutor = WebSocketServerManager.fileRecreatorBeanExecutor();
        fileRecreatorBeanExecutor.setPort(port); //optional default 9000
        //fileRecreatorBeanExecutor.setKeystorePassword("ssl-server.jks"); //optional default classpath: ssl-server.jks
        //fileRecreatorBeanExecutor.setKeystorePassword("password");
        fileRecreatorBeanExecutor.setPath("/incoming-data-app/routerBodyBinary");
        return fileRecreatorBeanExecutor;
    }

    @Bean
    public IncomingDataAppResourceOverWs incomingDataAppResourceOverWs(){
        IncomingDataAppResourceOverWs incomingDataAppResourceOverWs = new IncomingDataAppResourceOverWs();
        WebSocketServerManager.getMessageWebSocketResponse().addPropertyChangeListener(incomingDataAppResourceOverWs);
        return incomingDataAppResourceOverWs;
    }
}

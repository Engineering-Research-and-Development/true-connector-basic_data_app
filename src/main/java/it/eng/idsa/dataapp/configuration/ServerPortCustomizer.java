package it.eng.idsa.dataapp.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * @author Antonio Scatoloni on 24/04/2020
 **/

@Component
public class ServerPortCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Value("${application.websocket.isEnabled}")
    private boolean isEnabledWebSocket;
    @Value("${server.port}")
    private int port;
    @Value("${application.fileSenderPort}")
    private int fileSenderPort;

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        if (isEnabledWebSocket)
            factory.setPort(fileSenderPort); //0 switch off the Tomcat server
        else {
            factory.setPort(port);
        }
    }
}
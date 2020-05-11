package it.eng.idsa.dataapp.web.rest;

import it.eng.idsa.dataapp.service.impl.MultiPartMessageServiceImpl;
import it.eng.idsa.streamer.WebSocketClientManager;
import it.eng.idsa.streamer.websocket.receiver.server.FileRecreatorBeanExecutor;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Antonio Scatoloni
 */

@RestController
@EnableAutoConfiguration
@RequestMapping({"/"})
public class FileSenderResource {
    private static final Logger logger = LogManager.getLogger(FileSenderResource.class);

    @Autowired
    MultiPartMessageServiceImpl multiPartMessageService;

    @PostMapping("/sendFile")
    @ResponseBody
    public String sendFile(@RequestHeader("Forward-To-Internal") String forwardToInternal,
                           @RequestHeader("Forward-To") String forwardTo,
                           @RequestBody String fileName) throws Exception {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples-multipart-messages/" + fileName);
        String message = IOUtils.toString(is, "UTF8");
        FileRecreatorBeanExecutor.getInstance().setForwardTo(forwardTo);
        return WebSocketClientManager.getMessageWebSocketSender().sendMultipartMessageWebSocketOverHttps(message, forwardToInternal);
    }

}

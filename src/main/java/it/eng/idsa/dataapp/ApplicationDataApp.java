package it.eng.idsa.dataapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@EnableCaching
@SpringBootApplication
@EnableAutoConfiguration(exclude = {ErrorMvcAutoConfiguration.class})
public class ApplicationDataApp {

	@Value("${server.ssl.key-store}")
	private String keystore;
	@Value("${server.ssl.key-password}")
	private String password;
	@Value("${server.ssl.key-alias}")
	private String alias;
	@Value("${application.proxyPort}")
	private int proxyPort;
	@Value("${server.ssl.enabled:true}")
	private Boolean sslEnabled;

	private static final Logger logger = LoggerFactory.getLogger(ApplicationDataApp.class);

	public static void main(String[] args) {
	    System.setProperty("server.ssl.enabled", "true");
	    System.setProperty("application.ecc.protocol", "https");
	    System.setProperty("server.error.include-stacktrace", "never");
		SpringApplication.run(ApplicationDataApp.class, args);
	}

	@Bean
	public ServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();

		Arrays.asList(proxyPort).forEach(port -> {
			Connector connector = createSslConnector(port);
			tomcat.addAdditionalTomcatConnectors(connector);
		});

		return tomcat;
	}

	private Connector createSslConnector(int port) {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
		logger.info("Creating additional connector for port {}", port);
		try {
			if (sslEnabled) {
				File ks = new ClassPathResource(Paths.get(keystore).getFileName().toString()).getFile();
				logger.info("TLS enabled, adding to new connector");
//	        File truststore = new ClassPathResource("truststore.jks").getFile();
				connector.setScheme("https");
				connector.setSecure(true);
				protocol.setSSLEnabled(true);
				protocol.setKeystoreFile(ks.getAbsolutePath());
				protocol.setKeystorePass(password);
				protocol.setKeyAlias(alias);
//	        protocol.setTruststoreFile(truststore.getAbsolutePath());
//	        protocol.setTruststorePass(password);
			}
			connector.setPort(port);
			return connector;
		} catch (IOException ex) {
			throw new IllegalStateException(
					"can't access keystore: [" + "keystore" + "] or truststore: [" + "keystore" + "]", ex);
		}
	}
}

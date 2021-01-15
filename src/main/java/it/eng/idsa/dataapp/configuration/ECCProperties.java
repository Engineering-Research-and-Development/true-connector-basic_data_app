package it.eng.idsa.dataapp.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("application.ecc")
public class ECCProperties {
	private String protocol;
	private String host;
	private int port;
	private String mixContext;
	private String formContext;
	private String headerContext;
	
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getMixContext() {
		return mixContext;
	}
	public void setMixContext(String mixContext) {
		this.mixContext = mixContext;
	}
	public String getFormContext() {
		return formContext;
	}
	public void setFormContext(String formContext) {
		this.formContext = formContext;
	}
	public String getHeaderContext() {
		return headerContext;
	}
	public void setHeaderContext(String headerContext) {
		this.headerContext = headerContext;
	}
	
}

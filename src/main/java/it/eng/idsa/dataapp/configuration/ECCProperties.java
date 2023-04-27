package it.eng.idsa.dataapp.configuration;

import javax.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties("application.ecc")
public class ECCProperties {
	
	@NotEmpty
	private String protocol;
	@NotEmpty
	private String host;
	private int port;
	@NotEmpty
	private String mixContext;
	@NotEmpty
	private String formContext;
	@NotEmpty
	private String headerContext;
	
	private int selfdescriptionPort;
	
	@NotEmpty
	private String brokerRegisterContext;
	@NotEmpty
	private String brokerUpdateContext;
	@NotEmpty
	private String brokerDeleteContext;
	@NotEmpty
	private String brokerPassivateContext;
	@NotEmpty
	private String brokerQuerryContext;
	
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
	
	public int getSelfdescriptionPort() {
		return selfdescriptionPort;
	}
	public void setSelfdescriptionPort(int selfdescriptionPort) {
		this.selfdescriptionPort = selfdescriptionPort;
	}
	
	public String getBrokerRegisterContext() {
		return brokerRegisterContext;
	}
	public void setBrokerRegisterContext(String brokerRegisterContext) {
		this.brokerRegisterContext = brokerRegisterContext;
	}
	public String getBrokerUpdateContext() {
		return brokerUpdateContext;
	}
	public void setBrokerUpdateContext(String brokerUpdateContext) {
		this.brokerUpdateContext = brokerUpdateContext;
	}
	public String getBrokerDeleteContext() {
		return brokerDeleteContext;
	}
	public void setBrokerDeleteContext(String brokerDeleteContext) {
		this.brokerDeleteContext = brokerDeleteContext;
	}
	public String getBrokerPassivateContext() {
		return brokerPassivateContext;
	}
	public void setBrokerPassivateContext(String brokerPassivateContext) {
		this.brokerPassivateContext = brokerPassivateContext;
	}
	public String getBrokerQuerryContext() {
		return brokerQuerryContext;
	}
	public void setBrokerQuerryContext(String brokerQuerryContext) {
		this.brokerQuerryContext = brokerQuerryContext;
	}
}

package it.eng.idsa.dataapp.domain;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Class to wrap up data needed for proxy request logic
 * @author igor.balog
 *
 */
public class ProxyRequest {
	public static final String MULTIPART_MIXED = "mixed";
	public static final String MULTIPART_FORM = "form";
	public static final String MULTIPART_HEADER = "http-header";
	public static final String WSS = "wss";

	private String multipart;
	private String forwardTo;
	private String forwardToInternal;
	private String message;
	private String payload;
	private String requestedArtifact;
	private Map<String, Object> messageAsHeader;
	
	public ProxyRequest() {
		super();
	}

	public ProxyRequest(String multipart, String forwardTo, String forwardToInternal, String message, String payload,
			String requestedArtifact, Map<String, Object> messageAsHeader) {
		super();
		this.multipart = multipart;
		this.forwardTo = forwardTo;
		this.forwardToInternal = forwardToInternal;
		this.message = message;
		this.payload = payload;
		this.requestedArtifact = requestedArtifact;
		this.messageAsHeader = messageAsHeader;
	}

	public String getMultipart() {
		return multipart;
	}
	public void setMultipart(String multipart) {
		this.multipart = multipart;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getPayload() {
		return payload;
	}
	public void setPayload(String payload) {
		this.payload = payload;
	}
	public String getRequestedArtifact() {
		return requestedArtifact;
	}
	public void setRequestedArtifact(String requestedArtifact) {
		this.requestedArtifact = requestedArtifact;
	}
	public Map<String, Object> getMessageAsHeader() {
		return messageAsHeader;
	}
	public void setMessageAsHeader(Map<String, Object> messageAsHeader) {
		this.messageAsHeader = messageAsHeader;
	}

	public String getForwardTo() {
		return forwardTo;
	}

	public void setForwardTo(String forwardTo) {
		this.forwardTo = forwardTo;
	}

	public String getForwardToInternal() {
		return forwardToInternal;
	}

	public void setForwardToInternal(String forwardToInternal) {
		this.forwardToInternal = forwardToInternal;
	}
	 
	public String toString() {
	        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
	            .append("multipart", multipart)
	            .append("Forwrd-To", forwardTo)
	            .append("Forward-To-Internal", forwardToInternal)
	            .append("requestedArtifact", requestedArtifact)
	            .append("message", message)
	            .append("payload", payload)
	            .append("msgAsHeaders", messageAsHeader != null ? messageAsHeader.keySet().stream()
	            		.map(key -> key + "=" + (String) messageAsHeader.get(key))
	            	    .collect(Collectors.joining(", ", "{", "}"))
	            	    : null)
	            .toString();
	    }
}

package it.eng.idsa.dataapp.web.rest;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import it.eng.idsa.dataapp.service.ProxyService;

@Controller
public class ProxyController {
	
	private static final Logger logger = LogManager.getLogger(ProxyController.class);

	private static final String MULTIPART = "MULTIPART";
	private static final String MULTIPART_MIXED = "mixed";
	private static final String MULTIPART_FORM = "form";
	private static final String MULTIPART_HEADER = "http-header";

	private ProxyService proxySrvice;

	public ProxyController(ProxyService proxySrvice) {
		this.proxySrvice = proxySrvice;
	}

	@RequestMapping("/proxy")
	public ResponseEntity<?> proxyRequest(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody(required = false) String body, HttpMethod method, HttpServletRequest request,
			HttpServletResponse response) throws URISyntaxException, IOException {

		String type = httpHeaders.getFirst(MULTIPART);
		logger.debug("Type: " + type);

		switch (type) {
		case MULTIPART_MIXED:
			logger.info("Forwarding request using {}", MULTIPART_MIXED);
			return proxySrvice.proxyMultipartMix(body, httpHeaders);
		case MULTIPART_FORM:
			logger.info("Forwarding request using {}", MULTIPART_FORM);
			return proxySrvice.proxyMultipartForm(body, httpHeaders);
		case MULTIPART_HEADER:
			logger.info("Forwarding request using {}", MULTIPART_HEADER);
			return proxySrvice.proxyHttpHeader(body, httpHeaders);
		default:
			return new ResponseEntity<>("Missing proper header value - MULTIPART", HttpStatus.BAD_REQUEST);
		}
	}
}

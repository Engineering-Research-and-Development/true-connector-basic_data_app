package it.eng.idsa.dataapp.web.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.eng.idsa.dataapp.domain.ProxyRequest;
import it.eng.idsa.dataapp.service.ProxyService;

@RestController
public class ProxyController {
	
	private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

	private ProxyService proxyService;

	public ProxyController(ProxyService proxyService) {
		this.proxyService = proxyService;
	}

	/**
	 * Unique entry point in data App for proxying multipart mixed, multipart form, http-header and wss requestss towards ECC
	 * @param httpHeaders
	 * @param body - json representation containing information needed for correct forwarding
	 * @param method
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/proxy")
	public ResponseEntity<?> proxyRequest(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody String body, HttpMethod method) throws Exception {

		ProxyRequest proxyRequest = proxyService.parseIncommingProxyRequest(body);
		logger.info("Type: " + proxyRequest.getMultipart());
		logger.debug("Parsed proxy request: " + proxyRequest);
		if(StringUtils.isEmpty(proxyRequest.getMultipart())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Multipart field not found in request, mandatory for the flow");
		}
		
		switch (proxyRequest.getMultipart()) {
		case ProxyRequest.MULTIPART_MIXED:
			logger.info("Forwarding request using {}", ProxyRequest.MULTIPART_MIXED);
			return proxyService.proxyMultipartMix(proxyRequest, httpHeaders);
		case ProxyRequest.MULTIPART_FORM:
			logger.info("Forwarding request using {}", ProxyRequest.MULTIPART_FORM);
			return proxyService.proxyMultipartForm(proxyRequest, httpHeaders);
		case ProxyRequest.MULTIPART_HEADER:
			logger.info("Forwarding request using {}", ProxyRequest.MULTIPART_HEADER);
			return proxyService.proxyHttpHeader(proxyRequest, httpHeaders);
		case ProxyRequest.WSS:
			logger.info("Forwarding request using {}", ProxyRequest.WSS);
			if (StringUtils.isNotBlank(proxyRequest.getRequestedArtifact())) {
				return proxyService.requestArtifact(proxyRequest);
			} else {
				return proxyService.proxyWSSRequest(proxyRequest);
			}
		default:
			logger.info("Wrong value for multipart field '{}'", proxyRequest.getMultipart());
			return new ResponseEntity<>("Missing proper value for MULTIPART, should be one of: '" + ProxyRequest.MULTIPART_MIXED + 
					"', '" + ProxyRequest.MULTIPART_FORM + "', '" + ProxyRequest.MULTIPART_HEADER + "'", 
					HttpStatus.BAD_REQUEST);
		}
	}
}

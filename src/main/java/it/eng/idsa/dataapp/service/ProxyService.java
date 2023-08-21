package it.eng.idsa.dataapp.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import it.eng.idsa.dataapp.domain.ProxyRequest;

public interface ProxyService {
	/**
	 * Creates multipart-mixed request and sends it to Sender ECC
	 * 
	 * @param proxyRequest - contains message type, payload and other required data
	 *                     for flow
	 * @param httpHeaders  - original headers from request; will be forwarded as-is
	 * @return Response entity
	 * @throws URISyntaxException exception
	 * @throws IOException        exception
	 */
	ResponseEntity<String> proxyMultipartMix(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException, IOException;

	/**
	 * Creates multipart-form request and sends it to Sender ECC
	 * 
	 * @param proxyRequest - contains message type, payload and other required data
	 *                     for flow
	 * @param httpHeaders  - original headers from request; will be forwarded as-is
	 * @return Response entity
	 * @throws URISyntaxException exception
	 * @throws IOException        exception
	 * 
	 */
	ResponseEntity<String> proxyMultipartForm(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException, IOException;

	/**
	 * Creates http-header request and sends it to Sender ECC
	 * 
	 * @param proxyRequest - contains message type, payload and other required data
	 *                     for flow
	 * @param httpHeaders  - original headers from request; will be forwarded as-is
	 * @return Response entity
	 * @throws URISyntaxException exception
	 */
	ResponseEntity<String> proxyHttpHeader(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException;

	ProxyRequest parseIncommingProxyRequest(String body);

	ResponseEntity<String> proxyWSSRequest(ProxyRequest proxyRequest) throws IOException, URISyntaxException;
}

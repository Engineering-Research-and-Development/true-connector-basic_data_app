package it.eng.idsa.dataapp.service;

import java.net.URISyntaxException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public interface ProxyService {
	
	ResponseEntity<String> proxyMultipartMix(String body, HttpHeaders headers) throws URISyntaxException;
	ResponseEntity<String> proxyMultipartForm(String body, HttpHeaders headers) throws URISyntaxException;
	ResponseEntity<String> proxyHttpHeader(String body, HttpHeaders headers) throws URISyntaxException ;
}

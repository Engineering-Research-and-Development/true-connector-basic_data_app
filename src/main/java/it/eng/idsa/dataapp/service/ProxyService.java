package it.eng.idsa.dataapp.service;

import java.net.URISyntaxException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import it.eng.idsa.dataapp.domain.ProxyRequest;

public interface ProxyService {
	
	ResponseEntity<String> proxyMultipartMix(String body, HttpHeaders httpHeaders) throws URISyntaxException;
	ResponseEntity<String> proxyMultipartForm(String body, HttpHeaders httpHeaders) throws URISyntaxException;
	ResponseEntity<String> proxyHttpHeader(String body, HttpHeaders httpHeaders) throws URISyntaxException;
	
	ResponseEntity<String> proxyMultipartMix(ProxyRequest proxyRequest, HttpHeaders httpHeaders) throws URISyntaxException;
	ResponseEntity<String> proxyMultipartForm(ProxyRequest proxyRequest, HttpHeaders httpHeaders) throws URISyntaxException;
	ResponseEntity<String> proxyHttpHeader(ProxyRequest proxyRequest, HttpHeaders httpHeaders) throws URISyntaxException;
	
	ProxyRequest parseIncommingProxyRequest(String body);
}
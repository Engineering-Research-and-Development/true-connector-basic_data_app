package it.eng.idsa.dataapp.web.rest;

import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import it.eng.idsa.dataapp.watchdir.ArtifactRepository;

@RestController
public class ResourceController {

	private static final Logger logger = LogManager.getLogger(ResourceController.class);
	
	@Autowired
	private ArtifactRepository repo;
	
	@GetMapping("/resource")
	public ResponseEntity<String> getResource() {
		String body = 
				repo.getIndex().keySet().stream().map(key -> key.getName() + " = " + repo.getIndex().get(key) + System.lineSeparator())
				.collect(Collectors.joining(", ", "{", "}"));
//				.keySet()
//				.stream()
//			      .map(key -> key + "=" + map.get(key))
//			      .collect(Collectors.joining(", ", "{", "}"));
		
		return ResponseEntity.ok().body(body);//.build();
	}
}

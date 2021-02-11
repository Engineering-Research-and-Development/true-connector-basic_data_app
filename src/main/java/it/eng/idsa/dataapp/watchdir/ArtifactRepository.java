package it.eng.idsa.dataapp.watchdir;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class ArtifactRepository {

	private Map<File, String> index = new HashMap<>();

	public void addArtifact(File artifact) {
		index.put(artifact, UUID.randomUUID().toString());
	}

	public void removeArtifact(File artifact) {
		index.remove(artifact);
	}
	
	public Map<File, String> getIndex() {
		return Collections.unmodifiableMap(index);
	}
}

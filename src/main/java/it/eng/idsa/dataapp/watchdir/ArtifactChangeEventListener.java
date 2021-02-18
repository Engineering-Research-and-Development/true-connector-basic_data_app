package it.eng.idsa.dataapp.watchdir;

import java.nio.file.StandardWatchEventKinds;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ArtifactChangeEventListener {

	private static final Logger logger = LogManager.getLogger(ArtifactChangeEventListener.class);
	
	private ArtifactRepository repo;
	
	public ArtifactChangeEventListener(ArtifactRepository repo) {
		this.repo = repo;
	}
	
	@EventListener
	public void onApplicationEvent(ArtifactChangeEvent event) {
		logger.info(event.getArtifact().getName() + " event is " + event.getEventType().toString());
		if(StandardWatchEventKinds.ENTRY_DELETE.equals(event.getEventType())) {
			repo.removeArtifact(event.getArtifact());
		} else if(StandardWatchEventKinds.ENTRY_CREATE.equals(event.getEventType())) {
			repo.addArtifact(event.getArtifact());
		}
	}
}

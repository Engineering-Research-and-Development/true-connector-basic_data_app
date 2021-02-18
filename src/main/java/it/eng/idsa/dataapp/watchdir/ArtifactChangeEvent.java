package it.eng.idsa.dataapp.watchdir;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;

import org.springframework.context.ApplicationEvent;

public class ArtifactChangeEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;
	private File artifact;
	private Kind<Path> eventType;

	public ArtifactChangeEvent(Object source, File artifact, Kind<Path> eventType) {
		super(source);
		this.artifact = artifact;
		this.eventType = eventType;
	}

	public File getArtifact() {
		return artifact;
	}
	
	public Kind<Path> getEventType () {
		return eventType;
	}
	
}

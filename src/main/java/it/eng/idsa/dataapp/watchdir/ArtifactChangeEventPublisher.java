package it.eng.idsa.dataapp.watchdir;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ArtifactChangeEventPublisher {

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public void publishArtifactChangeEvent(final File artifact, final Kind<Path> entryCreate) {
		System.out.println("Publishing custom event. ");
		ArtifactChangeEvent artifactChangeEvent = new ArtifactChangeEvent(this, artifact, entryCreate);
		applicationEventPublisher.publishEvent(artifactChangeEvent);
	}
}

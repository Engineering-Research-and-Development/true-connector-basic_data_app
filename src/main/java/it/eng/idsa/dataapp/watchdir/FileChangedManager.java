package it.eng.idsa.dataapp.watchdir;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class FileChangedManager implements FileChangedListener {

	private static final Logger logger = LogManager.getLogger(FileChangedManager.class);
	private ArtifactRepository repo;
	
	public FileChangedManager(ArtifactRepository repo) {
		this.repo = repo;
	}

	@Override
	public void notifyAdd(File artifact) {
		logger.info("File chaned ADD {}", artifact);
		repo.addArtifact(artifact);
	}

	@Override
	public void notifyRemove(File artifact) {
		logger.info("File chaned REMOVED {}", artifact);
		repo.removeArtifact(artifact);
	}

}

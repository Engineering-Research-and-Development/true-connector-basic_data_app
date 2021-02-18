package it.eng.idsa.dataapp.watchdir;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DirectoryWatcher implements Runnable {

	private static final Logger logger = LogManager.getLogger(DirectoryWatcher.class);

	private WatchService watchService;
	private String watchDirectory;
	
	private ArtifactChangeEventPublisher publisher;
	
	public DirectoryWatcher(@Value("${application.watchdir}") String watchDirectory, ArtifactChangeEventPublisher publisher) 
			throws IOException {
		this.watchDirectory = watchDirectory;
        watchService = FileSystems.getDefault().newWatchService();
        Path path = createDirIfNotExists(watchDirectory);
        path.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE);
        
		this.publisher = publisher;

		notifyListenersOfExistingFiles();
        logger.info("Watching directory '" + watchDirectory + "' for artifacts.");
    }
	
	 private Path createDirIfNotExists(String watchDirectory) {
	        Path path = Paths.get(watchDirectory);
	        if (!path.toFile().exists()) path.toFile().mkdirs();
	        return path;
	    }


	@Override
	public void run() {
		 WatchKey key;
	        try {
	            while ((key = watchService.take()) != null) {
	                for (WatchEvent<?> event : key.pollEvents()) {
	                    File affectedFile = new File(watchDirectory + File.separator + event.context());
	                    if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE) && affectedFile.isFile()) {
	                    	publisher.publishArtifactChangeEvent(affectedFile, StandardWatchEventKinds.ENTRY_CREATE);
	                    } else {
	                        if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
	                        	publisher.publishArtifactChangeEvent(affectedFile, StandardWatchEventKinds.ENTRY_DELETE);
	                        }
	                    }
	                }
	                key.reset();
	            }
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	            logger.info("Watching for artifacts interrupted.");
	        }
	}
	
	 private void notifyListenersOfExistingFiles() {
		 logger.info("Reading existing artifact from watchDirectory {}", watchDirectory);
	        File dir = new File(watchDirectory);
	        for (File oldFile : dir.listFiles()) {
	        	publisher.publishArtifactChangeEvent(oldFile, StandardWatchEventKinds.ENTRY_CREATE);
	        }
	    }
}

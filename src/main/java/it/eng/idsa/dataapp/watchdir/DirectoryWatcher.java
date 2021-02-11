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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DirectoryWatcher implements Runnable {

	private static final Logger logger = LogManager.getLogger(DirectoryWatcher.class);

	private WatchService watchService;
	private String watchDirectory;
	private List<FileChangedListener> listeners = new ArrayList<>();
	
	public DirectoryWatcher(@Value("${application.watchdir}") String watchDirectory, FileChangedListener fcManager) throws IOException {
		this.watchDirectory = watchDirectory;
        watchService = FileSystems.getDefault().newWatchService();
        Path path = createDirIfNotExists(watchDirectory);
        path.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE);
        
        listeners = Arrays.asList(fcManager);
		notifyListenersOfExistingFiles(listeners);

        logger.info("Watching directory '" + watchDirectory + "' for artifacts.");
    }
	
	 private Path createDirIfNotExists(String watchDirectory) {
	        Path path = Paths.get(watchDirectory);
	        if (!path.toFile().exists()) path.toFile().mkdirs();
	        return path;
	    }

	public void setListeners(List<FileChangedListener> listeners) {
		this.listeners = listeners;
		notifyListenersOfExistingFiles(listeners);
	}

	@Override
	public void run() {
		 WatchKey key;
	        try {
	            while ((key = watchService.take()) != null) {
	                for (WatchEvent<?> event : key.pollEvents()) {
	                    File affectedFile = new File(watchDirectory + File.separator + event.context());
	                    if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE) && affectedFile.isFile()) {
	                    	 
	                        listeners.forEach(listener -> {
//	                            try {
	                                listener.notifyAdd(affectedFile);
//	                            } catch (InfomodelFormalException | IOException e) {
//	                                logger.error("Could not update new artifact information at broker.", e);
//	                                //e.printStackTrace();
//	                            }
	                        });
	                    } else {
	                        if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
	                            listeners.forEach(listener -> {
//	                                try {
	                                    listener.notifyRemove(affectedFile);
//	                                } catch (InfomodelFormalException | IOException e) {
//	                                    logger.error("Could not update new artifact information at broker.", e);
//	                                    //e.printStackTrace();
//	                                }
	                            });
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
	
	 private void notifyListenersOfExistingFiles(Collection<FileChangedListener> listeners) {
		 logger.info("Reading existing artifact from watchDirectory {}", watchDirectory);
	        File dir = new File(watchDirectory);
	        for (File oldFile : dir.listFiles()) {
	            listeners.forEach(listener -> {
                    listener.notifyAdd(oldFile);
	            });
	        }
	    }
}

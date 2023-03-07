package it.eng.idsa.dataapp.service;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * Service Interface for managing RecreateFileService.
 */
public interface RecreateFileService {
	
	/**
	 * Save file using default fileName and path
	 * @param payload payload
	 * @throws IOException exception
	 */
	void recreateTheFile(String payload) throws IOException;
	
	/**
	 * Save file with provided fileName and path
	 * @param payload payload
	 * @param targetFile target file
	 * @throws IOException exception
	 */
	void recreateTheFile(String payload, File targetFile) throws IOException;

}

package it.eng.idsa.dataapp.service;

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
	
	public void recreateTheFile(String payload) throws IOException;

}

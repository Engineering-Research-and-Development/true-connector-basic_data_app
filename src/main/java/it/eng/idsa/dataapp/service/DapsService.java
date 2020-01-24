package it.eng.idsa.dataapp.service;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * Service Interface for managing DAPS.
 */
public interface DapsService {

	public String getJwtToken();
	
	public boolean validateToken(String tokenValue);
}

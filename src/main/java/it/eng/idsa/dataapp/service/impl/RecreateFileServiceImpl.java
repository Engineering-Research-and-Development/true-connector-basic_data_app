package it.eng.idsa.dataapp.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.eng.idsa.dataapp.service.RecreateFileService;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */


/**
 * Service Implementation for managing RecreateFileService.
 */
@Service
@Transactional
public class RecreateFileServiceImpl implements RecreateFileService {
	
	private static final String FILE_PATH = "src\\main\\resources\\received-fiels\\";
	private static final String FILE_NAME = "Engineering-COPY.pdf";
	
	@Override
	public void recreateTheFile(String payload) throws IOException {
		String payloadCleaned = payload.replaceAll(System.lineSeparator(), "");
		File targetFile = new File(FILE_PATH + FILE_NAME);
		FileOutputStream fos = new FileOutputStream(targetFile);
		byte[] decoder = Base64.getDecoder().decode(payloadCleaned);
		fos.write(decoder);
	}
}

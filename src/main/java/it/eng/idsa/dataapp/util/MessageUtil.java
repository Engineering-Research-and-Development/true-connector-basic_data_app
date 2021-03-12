package it.eng.idsa.dataapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MessageUtil {
	
	
	private static final Logger logger = LogManager.getLogger(MessageUtil.class);
	
	public static String createResponsePayload() {
		// Put check sum in the payload
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String formattedDate = dateFormat.format(date);
		
		 Map<String, String> jsonObject = new HashMap<>();
         jsonObject.put("firstName", "John");
         jsonObject.put("lastName", "Doe");
         jsonObject.put("dateOfBirth", formattedDate);
         jsonObject.put("address", "591  Franklin Street, Pennsylvania");
         jsonObject.put("checksum", "ABC123 " + formattedDate);
         Gson gson = new GsonBuilder().create();
         return gson.toJson(jsonObject);
	}
	
	public static String createContractAgreement() {
		String contractAgreement = null;
		
		try (InputStream is = MessageUtil.class.getClassLoader().getResourceAsStream("dataFiles/contract_agreement.json")){
			contractAgreement = IOUtils.toString(is, "UTF8");
		} catch (IOException e) {
			logger.error("Could not get contract agreement "+e.getMessage());
		} 
		
		return contractAgreement;
				
				
	}
	

}

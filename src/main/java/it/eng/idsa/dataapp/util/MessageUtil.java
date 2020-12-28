package it.eng.idsa.dataapp.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MessageUtil {
	
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
	

}

package it.eng.idsa.dataapp.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PayloadUtil {
	
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
	
	public static HttpHeaders createHttpHeaderResponseHeaders() {
		HttpHeaders headers = new HttpHeaders();

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date date = new Date();
		String formattedDate = dateFormat.format(date);

		headers.add("IDS-Messagetype", "ids:ArtifactResponseMessage");
		headers.add("IDS-Id", "https://w3id.org/idsa/autogen/artifactResponseMessage/" + UUID.randomUUID().toString());
		headers.add("IDS-Issued", formattedDate);
		headers.add("IDS-ModelVersion", "4.0.0");
		headers.add("IDS-IssuerConnector", "http://iais.fraunhofer.de/ids/mdm-connector");
//				headers.add("IDS-TransferContract", "https://mdm-connector.ids.isst.fraunhofer.de/examplecontract/bab-bayern-sample/");
//				headers.add("IDS-CorrelationMessage", "http://industrialdataspace.org/connectorUnavailableMessage/1a421b8c-3407-44a8-aeb9-253f145c869a");

		return headers;
	}

}

package it.eng.idsa.dataapp.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:users.properties")
public class UserProperties {

	private final Map<String, String> userCredentials = new HashMap<String, String>();

	public UserProperties(Environment env) {
		String usersList = env.getProperty("users.list");
		if (usersList != null) {
			for (String user : usersList.split(",")) {
				String username = user.trim();
				String password = env.getProperty(username + ".password");
				userCredentials.put(username, password);
			}
		}
	}

	public String getPasswordForUser(String user) {
		return userCredentials.get(user);
	}

	public Map<String, String> getUserCredentials() {
		return userCredentials;
	}
}

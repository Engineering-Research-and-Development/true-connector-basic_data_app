package it.eng.idsa.dataapp.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationSuccessEventListener.class);

	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {

		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) event.getSource();
		User user = (User) token.getPrincipal();
		logger.info("Successful login for user: " + user.getUsername());
	}

}

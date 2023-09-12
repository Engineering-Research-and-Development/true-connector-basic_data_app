package it.eng.idsa.dataapp.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFailureListener.class);

	@Override
	public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
		logger.error(
				"Failed login for user: " + ((UsernamePasswordAuthenticationToken) event.getSource()).getPrincipal()
						+ ", reason: " + event.getException().getLocalizedMessage());
	}

}

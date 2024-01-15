package it.eng.idsa.dataapp.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	private final UserProperties userProperties;

	public SecurityConfig(UserProperties userProperties) {
		this.userProperties = userProperties;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		userProperties.getUserCredentials().keySet().forEach(user -> {
			String password = userProperties.getPasswordForUser(user);
			if (password != null) {
				try {
					auth.inMemoryAuthentication().withUser(user).password(password).roles("PROXY");
				} catch (Exception e) {
					logger.error("Error configuring authentication for user " + user, e.getMessage());
				}
			}
		});
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				// HTTP Basic authentication
				.httpBasic().and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.authorizeRequests().antMatchers("/proxy").hasRole("PROXY").antMatchers("/about/**").permitAll()
				.antMatchers("/error").permitAll().antMatchers("/data").permitAll()
				.antMatchers("/incoming-data-app/routerBodyBinary").permitAll().and().csrf().disable().formLogin()
				.disable().headers().xssProtection().and().contentTypeOptions().and().frameOptions().sameOrigin();
	}

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
}
package it.eng.idsa.dataapp.configuration;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	@Value("${application.cors.allowed.origins:}")
	private String allowedOrigins;

	@Value("${application.cors.allowed.methods:}")
	private String allowedMethods;

	@Value("${application.cors.allowed.headers:}")
	private String allowedHeaders;

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
		http.cors().and()
		.csrf().disable().httpBasic().and()
		.sessionManagement()
		.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
		.authorizeRequests().antMatchers("/proxy")
		.hasRole("PROXY").antMatchers("/about/**").permitAll().antMatchers("/error").permitAll()
		.antMatchers("/data").permitAll().antMatchers("/incoming-data-app/routerBodyBinary").permitAll().and()
		.formLogin().disable().headers().xssProtection().and().contentTypeOptions().and().frameOptions()
		.sameOrigin();
	}

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		if (StringUtils.isBlank(allowedOrigins)) {
			configuration.addAllowedOrigin("*");
		} else {
			configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
		}

		if (StringUtils.isBlank(allowedMethods)) {
			configuration.addAllowedMethod("*");
		} else {
			configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
		}

		if (StringUtils.isBlank(allowedHeaders)) {
			configuration.addAllowedHeader("*");
		} else {
			configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
		}

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
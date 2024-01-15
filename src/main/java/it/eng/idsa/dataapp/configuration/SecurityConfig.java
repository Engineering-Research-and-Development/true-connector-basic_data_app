package it.eng.idsa.dataapp.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@PropertySource("classpath:users.properties")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private Environment env;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		String usersList = env.getProperty("users.list");
		if (usersList != null && !usersList.isEmpty()) {
			String[] users = usersList.split(",");
			for (String user : users) {
				String password = env.getProperty(user.trim() + ".password");
				if (password != null) {
					auth.inMemoryAuthentication().withUser(user.trim()).password(password).roles("PROXY");
				}
			}
		}
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
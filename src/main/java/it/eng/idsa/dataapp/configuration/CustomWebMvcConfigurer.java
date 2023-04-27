package it.eng.idsa.dataapp.configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Configuration
public class CustomWebMvcConfigurer implements WebMvcConfigurer {
	
	@Value("${application.proxyPort}")
	private int proxyPort;
	
	@Value("${server.port}")
	private int serverPort;
	
	class HandlerInterceptor extends HandlerInterceptorAdapter {

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

			String handlerName = ((HandlerMethod) handler).getBean().getClass().getName();
			if (request.getLocalPort() == proxyPort && handlerName.contains("Proxy")) {
				return true;
			}

			if (request.getLocalPort() != proxyPort && handlerName.contains("Proxy")) {
				response.setStatus(HttpStatus.FORBIDDEN_403);
				return false;
			}
			
			return true;
		}
	}

	@Override
	public void addInterceptors(final InterceptorRegistry registry) {
		registry.addInterceptor(new HandlerInterceptor()).order(Ordered.LOWEST_PRECEDENCE);
	}
}

package com.redledger.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		/*
		 * VULN: [A5] — (3.A5.3) CORS misconfiguration.
		 * allowedOriginPatterns("*") combined with allowCredentials(true) allows any origin
		 * to make credentialed cross-origin requests (cookies, Authorization headers).
		 * allowedHeaders("*") exposes all request headers including sensitive ones.
		 * This enables CSRF-style attacks from attacker-controlled origins.
		 */
		registry.addMapping("/api/**")
			.allowedOriginPatterns("*")
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
			.allowedHeaders("*")
			.allowCredentials(true)
			.exposedHeaders("Authorization");
	}
}

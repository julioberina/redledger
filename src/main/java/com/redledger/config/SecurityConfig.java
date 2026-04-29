package com.redledger.config;

import com.redledger.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))  // ← add this
			)
			.authorizeHttpRequests(auth -> auth
				/*
				 * VULN: [A4] — (3.A4.1) No rate limiting or throttling on /api/auth/login. An attacker can
				 * make unlimited login attempts with no delay, lockout, or CAPTCHA. This enables brute-force
				 * and credential stuffing attacks against any account. No RateLimitFilter is registered in
				 * this filter chain — the absence of throttling is intentional for demonstration purposes.
				 */
				.requestMatchers("/api/auth/**").permitAll()
				/*
				 * VULN: [A5] — (3.A5.2) H2 console accessible to unauthenticated users with no IP restriction.
				 * Combined with web-allow-others=true, any remote attacker can reach the full database console.
				 */
				.requestMatchers("/h2-console/**").permitAll()
				// VULN: [A1] — /api/admin/** requires only authentication, not ADMIN role; BFLA via missing function-level authorization
				.requestMatchers("/api/admin/**").authenticated()
				.anyRequest().authenticated()
			)
			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}

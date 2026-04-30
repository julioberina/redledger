package com.redledger.security;

import com.redledger.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	private static final List<String> PUBLIC_PATHS = List.of(
		"/api/auth/**",
		"/h2-console/**"
	);
	private final JwtUtils jwtUtils;
	private final UserService userService;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	public JwtAuthenticationFilter(JwtUtils jwtUtils, UserService userService) {
		this.jwtUtils = jwtUtils;
		this.userService = userService;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader("Authorization");

		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);

			/*
			 * VULN: [A7] — (3.A7.3) Expired JWT tokens are accepted.
			 * The filter bypasses validateToken() and extracts claims directly, catching
			 * ExpiredJwtException to proceed with authentication anyway.
			 * Stolen tokens remain valid indefinitely past their expiry.
			 * CWE-613: Insufficient Session Expiration.
			 */
			String username = null;

			try {
				username = jwtUtils.getUsernameFromToken(token);
			} catch (Exception e) {
				log.warn("JWT parse error on request {}: {}", request.getRequestURI(), e.getMessage());
			}

			if (username != null) {
				UserDetails userDetails = userService.loadUserByUsername(username);
				UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(userDetails, null,
						userDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);
				log.debug("Authenticated user '{}' via JWT (expiry not enforced)", username);
			}
		}

		filterChain.doFilter(request, response);
	}
}
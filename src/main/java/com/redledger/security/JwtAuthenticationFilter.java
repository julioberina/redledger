package com.redledger.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	public JwtAuthenticationFilter(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
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

			if (jwtUtils.validateToken(token)) {
				String username = jwtUtils.getUsernameFromToken(token);
				String role = jwtUtils.getRoleFromToken(token);

				UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(username, null,
						List.of(new SimpleGrantedAuthority(role)));

				SecurityContextHolder.getContext().setAuthentication(authentication);
				log.debug("Authenticated user '{}' via JWT", username);
			} else
				log.warn("Invalid or expired JWT token on request: {}", request.getRequestURI());
		}

		filterChain.doFilter(request, response);
	}
}
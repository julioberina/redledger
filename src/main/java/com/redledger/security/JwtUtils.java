package com.redledger.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration:86400000}")
	private long jwtExpiration;

	private SecretKey signingKey;

	/*
	 * VULN: [A2] — (3.A2.1) JWT signing key is derived from a weak, hardcoded secret ("secret" in
	 * application.properties). An attacker who brute-forces or guesses the secret can forge arbitrary
	 * JWTs, including tokens with ROLE_ADMIN, bypassing all authentication and authorization controls.
	 */
	@PostConstruct
	public void init() {
		this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	/*
	 * VULN: [A4] — (3.A4.3) Tokens are issued with a 24-hour expiry and no revocation mechanism.
	 * There is no token blacklist, no refresh-token rotation, and no short-lived access token pattern.
	 * A stolen token grants full API access until expiry with no way to invalidate it server-side.
	 */
	public String generateToken(String username, String role) {
		return Jwts.builder()
			.subject(username)
			.claim("role", role)
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + jwtExpiration))
			.signWith(signingKey)
			.compact();
	}

	public String getUsernameFromToken(String token) {
		return parseClaims(token).getSubject();
	}

	public String getRoleFromToken(String token) {
		return parseClaims(token).get("role", String.class);
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token);

			return true;
		} catch (ExpiredJwtException e) {
			logger.warn("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.warn("JWT token is unsupported: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			logger.warn("JWT token is malformed: {}", e.getMessage());
		} catch (SignatureException e) {
			logger.warn("JWT signature validation failed: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.warn("JWT claims string is empty or null: {}", e.getMessage());
		}
		return false;
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(signingKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
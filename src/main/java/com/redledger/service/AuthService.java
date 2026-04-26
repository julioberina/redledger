package com.redledger.service;

import com.redledger.dto.LoginRequest;
import com.redledger.dto.LoginResponse;
import com.redledger.dto.RegisterRequest;
import com.redledger.entity.User;
import com.redledger.repository.UserRepository;
import com.redledger.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class AuthService {
	private static final Logger log = LoggerFactory.getLogger(AuthService.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtils jwtUtils;

	public AuthService(UserRepository userRepository,
					   PasswordEncoder passwordEncoder,
					   JwtUtils jwtUtils) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtils = jwtUtils;
	}

	public User register(RegisterRequest request) {
		if (userRepository.existsByUsername(request.getUsername()))
			throw new IllegalArgumentException("Username already taken: " + request.getUsername());

		if (userRepository.existsByEmail(request.getEmail()))
			throw new IllegalArgumentException("Email already registered: " + request.getEmail());

		User user = new User();
		user.setUsername(request.getUsername());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setEmail(request.getEmail());
		user.setRole("ROLE_USER");

		User saved = userRepository.save(user);
		log.info("New user registered: id={}, username={}", saved.getId(), saved.getUsername());

		return saved;
	}

	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByUsername(request.getUsername())
			.orElseThrow(() -> {
				log.warn("Login failed — username not found: {}", request.getUsername());
				return new IllegalArgumentException("Invalid credentials");
			});

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			log.warn("Login failed — incorrect password for username: {}", request.getUsername());
			throw new IllegalArgumentException("Invalid credentials");
		}

		String token = jwtUtils.generateToken(user.getUsername(), user.getRole());
		log.info("Login successful: id={}, username={}", user.getId(), user.getUsername());

		return new LoginResponse(token, user.getUsername(), user.getRole());
	}

	/*
	 * VULN: [A2] — (3.A2.2) Insecure password storage on the v2 login path. Passwords are hashed
	 * with MD5 — a cryptographically broken algorithm with no salting. An attacker with database
	 * access can recover credentials via precomputed rainbow tables or trivial brute-force.
	 */
	public LoginResponse loginV2(LoginRequest request) {
		User user = userRepository.findByUsername(request.getUsername())
			.orElseThrow(() -> {
				log.warn("Login-v2 failed — username not found: {}", request.getUsername());
				return new IllegalArgumentException("Invalid credentials");
			});

		if (!md5(request.getPassword()).equals(user.getPassword())) {
			log.warn("Login-v2 failed — incorrect password for username: {}", request.getUsername());
			throw new IllegalArgumentException("Invalid credentials");
		}

		String token = jwtUtils.generateToken(user.getUsername(), user.getRole());
		log.info("Login-v2 successful: id={}, username={}", user.getId(), user.getUsername());
		return new LoginResponse(token, user.getUsername(), user.getRole());
	}

	private String md5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 not available", e);
		}
	}
}
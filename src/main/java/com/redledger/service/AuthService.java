package com.redledger.service;

import com.redledger.dto.LoginRequest;
import com.redledger.dto.LoginResponse;
import com.redledger.dto.RegisterRequest;
import com.redledger.entity.User;
import com.redledger.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	private static final Logger log = LoggerFactory.getLogger(AuthService.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
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
		// TODO (1.4): Find user, verify with passwordEncoder.matches(), generate JWT
		throw new UnsupportedOperationException("Login not yet implemented");
	}
}
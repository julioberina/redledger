package com.redledger.controller;

import com.redledger.dto.LoginRequest;
import com.redledger.dto.LoginResponse;
import com.redledger.dto.RegisterRequest;
import com.redledger.entity.User;
import com.redledger.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
		try {
			User user = authService.register(request);
			return ResponseEntity.status(HttpStatus.CREATED)
				.body(Map.of(
					"message", "User registered successfully",
					"userId", user.getId()
				));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("error", ex.getMessage()));
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
		try {
			LoginResponse response = authService.login(request);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", ex.getMessage()));
		}
	}

	/*
	 * VULN: [A2] — (3.A2.2) /login-v2 uses MD5 password hashing via loginV2().
	 * Exists alongside the secure /login path to demonstrate insecure password storage.
	 */
	@PostMapping("/login-v2")
	public ResponseEntity<?> loginV2(@Valid @RequestBody LoginRequest request) {
		try {
			LoginResponse response = authService.loginV2(request);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", ex.getMessage()));
		}
	}
}

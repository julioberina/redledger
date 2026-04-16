package com.redledger.controller;

import com.redledger.entity.User;
import com.redledger.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final UserService userService;

	public AdminController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
		List<Map<String, Object>> users = userService.getAllUsers().stream()
			.map(user -> Map.<String, Object>of(
				"id", user.getId(),
				"username", user.getUsername(),
				"email", user.getEmail(),
				"role", user.getRole()
			))
			.collect(Collectors.toList());
		return ResponseEntity.ok(users);
	}

	@GetMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getUserById(@PathVariable Long id) {
		return userService.getUserById(id)
			.map(user -> ResponseEntity.ok(Map.of(
				"id", user.getId(),
				"username", user.getUsername(),
				"email", user.getEmail(),
				"role", user.getRole()
			)))
			.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
	}

	@PutMapping("/users/{id}/role")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
		return userService.getUserById(id)
			.map(user -> {
				user.setRole(body.get("role"));
				userService.createUser(user);
				return ResponseEntity.ok(Map.of("message", "Role updated successfully"));
			})
			.orElse(ResponseEntity.notFound().build());
	}
}

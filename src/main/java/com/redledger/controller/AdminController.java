package com.redledger.controller;

import com.redledger.dto.UpdateUserRoleRequest;
import com.redledger.entity.User;
import com.redledger.service.UserService;
import jakarta.validation.Valid;
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

	// TODO: Add @PreAuthorize("hasRole('ADMIN')") — intentionally missing for Phase 3 BFLA
	@GetMapping("/users")
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

	// TODO: Add @PreAuthorize("hasRole('ADMIN')") — intentionally missing for Phase 3 BFLA
	@GetMapping("/users/{id}")
	public ResponseEntity<?> getUserById(@PathVariable Long id) {
		return userService.getUserById(id)
			.map(user -> ResponseEntity.ok(Map.<String, Object>of(
				"id", user.getId(),
				"username", user.getUsername(),
				"email", user.getEmail(),
				"role", user.getRole()
			)))
			.orElse(ResponseEntity.notFound().build());
	}

	// TODO: Add @PreAuthorize("hasRole('ADMIN')") — intentionally missing for Phase 3 BFLA
	@DeleteMapping("/users/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
	}

	// TODO: Add @PreAuthorize("hasRole('ADMIN')") — intentionally missing for Phase 3 BFLA
	@PutMapping("/users/{id}/role")
	public ResponseEntity<?> updateUserRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
		return userService.updateUserRole(id, request.role())
			.map(user -> ResponseEntity.ok(Map.of("message", "Role updated successfully")))
			.orElse(ResponseEntity.notFound().build());
	}
}

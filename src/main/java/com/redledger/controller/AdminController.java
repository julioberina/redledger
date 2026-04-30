package com.redledger.controller;

import com.redledger.dto.UpdateUserRoleRequest;
import com.redledger.entity.User;
import com.redledger.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
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

	// VULN: [A1] — Missing @PreAuthorize("hasRole('ADMIN')"); any authenticated user can list all users (BFLA)
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

	// VULN: [A1] — Missing @PreAuthorize("hasRole('ADMIN')"); any authenticated user can view any user's details (BFLA)
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

	// VULN: [A1] — Missing @PreAuthorize("hasRole('ADMIN')"); any authenticated user can delete any user (BFLA)
	@DeleteMapping("/users/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
	}

	// VULN: [A1] — Missing @PreAuthorize("hasRole('ADMIN')"); any authenticated user can escalate any user's role (BFLA / Privilege Escalation)
	@PutMapping("/users/{id}/role")
	public ResponseEntity<?> updateUserRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
		return userService.updateUserRole(id, request.role())
			.map(user -> ResponseEntity.ok(Map.of("message", "Role updated successfully")))
			.orElse(ResponseEntity.notFound().build());
	}

	/*
	 * VULN: [A3] — (3.A3.3) User-supplied `filename` parameter is concatenated directly into a
	 * shell command executed via Runtime.exec(). An attacker can inject arbitrary OS commands
	 * e.g. `; whoami`, `; cat /etc/passwd`, or `; rm -rf /` by manipulating the filename parameter.
	 */
	@GetMapping("/export")
	public ResponseEntity<?> exportFile(@RequestParam String filename) {
		try {
			Process process = Runtime.getRuntime().exec("cat " + filename);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder output = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			return ResponseEntity.ok(Map.of("output", output.toString()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
		}
	}

	/*
	 * VULN: [A8] — (3.A8.2) Insecure deserialization.
	 * Accepts a raw Java serialized object and deserializes it with no validation,
	 * type filtering, or integrity check. Combined with commons-collections 3.2.1
	 * on the classpath, this endpoint is exploitable via a gadget chain for
	 * remote code execution (RCE). CWE-502: Deserialization of Untrusted Data.
	 */
	@PostMapping(value = "/import", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<?> importData(@RequestBody byte[] data) {
		try (ObjectInputStream ois = new ObjectInputStream(
			new java.io.ByteArrayInputStream(data))) {
			Object obj = ois.readObject();
			return ResponseEntity.ok(Map.of("message", "Import successful", "type", obj.getClass().getName()));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}

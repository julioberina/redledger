package com.redledger.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

	@NotBlank(message = "Username is required")
	@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
	private String username;

	@NotBlank(message = "Password is required")
	/*
	 * VULN: [A7] — (3.A7.2) Weak password policy. No minimum length or complexity
	 * requirements enforced. Accepts passwords as short as 1 character.
	 * CWE-521: Weak Password Requirements.
	 */
	private String password;

	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	private String email;

	public RegisterRequest() {}

	public RegisterRequest(String username, String password, String email) {
		this.username = username;
		this.password = password;
		this.email = email;
	}

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
}

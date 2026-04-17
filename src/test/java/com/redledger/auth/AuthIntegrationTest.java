package com.redledger.auth;

import com.redledger.dto.LoginRequest;
import com.redledger.dto.RegisterRequest;
import org.springframework.test.context.jdbc.Sql;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {"/reset.sql", "/data.sql"},
	config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
	executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AuthIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Test
	void register_withValidPayload_returns201() throws Exception {
		RegisterRequest req = new RegisterRequest("testuser", "password123", "testuser@email.com");

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(req)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.userId").exists())
			.andExpect(jsonPath("$.message").value("User registered successfully"));
	}

	@Test
	void register_withDuplicateUsername_returns409() throws Exception {
		RegisterRequest req = new RegisterRequest("jsmith", "password123", "john.smith@email.com");

		// alice already exists in data.sql seed data — second attempt must fail
		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(req)))
			.andExpect(status().isConflict());
	}

	@Test
	void register_withInvalidEmail_returns400() throws Exception {
		RegisterRequest req = new RegisterRequest("newuser", "password123", "not-an-email");

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(req)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void login_withValidCredentials_returns200AndToken() throws Exception {
		LoginRequest req = new LoginRequest("jsmith", "password123");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(req)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").isNotEmpty())
			.andExpect(jsonPath("$.username").value("jsmith"))
			.andExpect(jsonPath("$.role").exists());
	}

	@Test
	void login_withInvalidPassword_returns401() throws Exception {
		LoginRequest req = new LoginRequest("alice", "wrongpassword");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(req)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void login_withNonexistentUser_returns401() throws Exception {
		LoginRequest req = new LoginRequest("ghost", "password123");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(req)))
			.andExpect(status().isUnauthorized());
	}

	// -------------------------------------------------------------------------
	// Register → Login → Access protected endpoint (full flow)
	// -------------------------------------------------------------------------

	@Test
	void fullFlow_registerThenLoginThenAccessProtected_returns200() throws Exception {
		// 1. Register
		RegisterRequest reg = new RegisterRequest("flowuser", "password123", "flowuser@email.com");
		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(reg)))
			.andExpect(status().isCreated());

		// 2. Login — capture JWT
		LoginRequest login = new LoginRequest("flowuser", "password123");
		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(login)))
			.andExpect(status().isOk())
			.andReturn();

		String token = jsonMapper.readTree(
			loginResult.getResponse().getContentAsString()).get("token").asText();

		// 3. Access a protected endpoint with the real JWT
		mockMvc.perform(get("/api/accounts")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk());
	}

	@Test
	void accessProtected_withNoToken_returns401() throws Exception {
		mockMvc.perform(get("/api/accounts"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void accessProtected_withGarbageToken_returns401() throws Exception {
		mockMvc.perform(get("/api/accounts")
				.header("Authorization", "Bearer this.is.garbage"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "alice", roles = {"USER"})
	void adminEndpoint_withUserRole_returns403() throws Exception {
		mockMvc.perform(get("/api/admin/users"))
			.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "admin", roles = {"ADMIN"})
	void adminEndpoint_withAdminRole_returns200() throws Exception {
		mockMvc.perform(get("/api/admin/users"))
			.andExpect(status().isOk());
	}
}
package com.redledger.business;

import tools.jackson.databind.json.JsonMapper;
import com.redledger.dto.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BusinessIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	private String userToken;
	private String adminToken;

	@BeforeEach
	void setUp() throws Exception {
		userToken = obtainToken("jsmith", "password123");
		adminToken = obtainToken("admin", "admin123");
	}

	private String obtainToken(String username, String password) throws Exception {
		String body = """
                {"username": "%s", "password": "%s"}
                """.formatted(username, password);

		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andReturn();

		String response = result.getResponse().getContentAsString();
		return jsonMapper.readTree(response).get("token").asText();
	}

	@Test
	void getAccount_returnsOk() throws Exception {
		mockMvc.perform(get("/api/accounts/1")
				.header("Authorization", "Bearer " + userToken))
			.andExpect(status().isOk());
	}

	@Test
	void getAccount_notFound_returns404() throws Exception {
		mockMvc.perform(get("/api/accounts/9999")
				.header("Authorization", "Bearer " + userToken))
			.andExpect(status().isNotFound());
	}

	@Test
	void getAccountBalance_returnsOk() throws Exception {
		mockMvc.perform(get("/api/accounts/1/balance")
				.header("Authorization", "Bearer " + userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.balance").exists());
	}

	@Test
	void getAccountTransactions_returnsOk() throws Exception {
		mockMvc.perform(get("/api/accounts/1/transactions")
				.header("Authorization", "Bearer " + userToken))
			.andExpect(status().isOk());
	}

	@Test
	void transfer_validRequest_returnsCreated() throws Exception {
		TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("10.00"), "Test transfer");

		mockMvc.perform(post("/api/transactions")
				.header("Authorization", "Bearer " + userToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("COMPLETED"));
	}

	@Test
	void transfer_insufficientFunds_returnsBadRequest() throws Exception {
		TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("999999.00"), "Overdraft attempt");

		mockMvc.perform(post("/api/transactions")
				.header("Authorization", "Bearer " + userToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void transfer_missingToken_returnsUnauthorized() throws Exception {
		TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("10.00"), "No auth");

		mockMvc.perform(post("/api/transactions")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void getTransactionById_returnsOk() throws Exception {
		mockMvc.perform(get("/api/transactions/1")
				.header("Authorization", "Bearer " + userToken))
			.andExpect(status().isOk());
	}

	@Test
	void getTransactionById_notFound_returns404() throws Exception {
		mockMvc.perform(get("/api/transactions/9999")
				.header("Authorization", "Bearer " + userToken))
			.andExpect(status().isNotFound());
	}

	@Test
	void adminGetAllUsers_withAdminToken_returnsOk() throws Exception {
		mockMvc.perform(get("/api/admin/users")
				.header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isOk());
	}

	@Test
	void adminGetUserById_returnsOk() throws Exception {
		mockMvc.perform(get("/api/admin/users/1")
				.header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isOk());
	}

	@Test
	void adminDeleteUser_returnsOk() throws Exception {
		mockMvc.perform(delete("/api/admin/users/2")
				.header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("User deactivated successfully"));
	}

	@Test
	void adminUpdateUserRole_returnsOk() throws Exception {
		String body = """
                {"role": "ROLE_ADMIN"}
                """;

		mockMvc.perform(put("/api/admin/users/2/role")
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Role updated successfully"));
	}
}
package com.redledger.service;

import com.redledger.dto.AccountResponse;
import com.redledger.entity.Account;
import com.redledger.entity.AccountType;
import com.redledger.entity.User;
import com.redledger.repository.AccountRepository;
import com.redledger.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

	private static final Logger log = LoggerFactory.getLogger(AccountService.class);

	private final AccountRepository accountRepository;
	private final UserRepository userRepository;
	private final JdbcTemplate jdbcTemplate;

	public AccountService(AccountRepository accountRepository,
						  UserRepository userRepository,
						  JdbcTemplate jdbcTemplate) {
		this.accountRepository = accountRepository;
		this.userRepository = userRepository;
		this.jdbcTemplate = jdbcTemplate;
	}

	public Account createAccount(Long userId, AccountType accountType) {
		User owner = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		/*
		 * VULN: [A4] — (3.A4.2) Account numbers are generated sequentially (ACC00000001, ACC00000002, ...).
		 * An authenticated attacker can enumerate all valid account IDs by incrementing the suffix,
		 * enabling targeted IDOR attacks against accounts they don't own.
		 */
		String accountNumber = generateAccountNumber();

		Account account = new Account(accountNumber, accountType, BigDecimal.ZERO, owner);
		Account saved = accountRepository.save(account);

		log.info("Account created: id={}, number={}, type={}, owner={}",
			saved.getId(), saved.getAccountNumber(), saved.getAccountType(), userId);

		return saved;
	}

	public Optional<Account> getAccount(Long accountId) {
		// VULN: [A1] — No ownership check; any authenticated user can retrieve any account by ID (IDOR)
		return accountRepository.findById(accountId);
	}

	public List<Account> getAccountsByUser(Long userId) {
		// VULN: [A1] — userId taken from request param, not from authenticated principal; caller can pass any userId
		return accountRepository.findByOwnerId(userId);
	}

	public Optional<Account> getAccountByNumber(String accountNumber) {
		return accountRepository.findByAccountNumber(accountNumber);
	}

	public AccountResponse toAccountResponse(Account account) {
		AccountResponse response = new AccountResponse();
		response.setId(account.getId());
		response.setAccountNumber(account.getAccountNumber());
		response.setAccountType(account.getAccountType());
		response.setBalance(account.getBalance());
		response.setOwnerId(account.getOwner().getId());
		response.setOwnerUsername(account.getOwner().getUsername());
		response.setCreatedAt(account.getCreatedAt());
		return response;
	}

	/*
	 * VULN: [A3] — (3.A3.1) Raw user input concatenated directly into SQL query string.
	 * No parameterization, no input sanitization. Attacker can inject arbitrary SQL via the
	 * `name` parameter — e.g. `%' OR '1'='1` dumps all accounts regardless of ownership.
	 */
	public List<java.util.Map<String, Object>> searchAccounts(String name) {
		String query = "SELECT * FROM accounts WHERE account_number LIKE '%" + name + "%'";
		return jdbcTemplate.queryForList(query);
	}

	/*
	 * VULN: [A10] — (3.A10.1) SSRF via webhook callback URL.
	 * User-supplied URL is fetched server-side with no allowlist, blocklist, or
	 * scheme validation. Attacker can probe internal network resources including
	 * cloud metadata endpoints (http://169.254.169.254/latest/meta-data/),
	 * internal services (http://localhost:8080/h2-console), and private subnets.
	 * CWE-918: Server-Side Request Forgery.
	 */
	public String fetchWebhook(String webhookUrl) throws Exception {
		URL url = new URL(webhookUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod("GET");
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(3000);

		StringBuilder response = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			String line;

			while ((line = reader.readLine()) != null)
				response.append(line).append("\n");
		}

		return response.toString();
	}

	private String generateAccountNumber() {
		long next = accountRepository.findTopByOrderByIdDesc()
			.map(a -> Long.parseLong(a.getAccountNumber().substring(3)) + 1)
			.orElse(1L);
		return String.format("ACC%08d", next);
	}
}
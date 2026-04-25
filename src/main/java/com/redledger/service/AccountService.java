package com.redledger.service;

import com.redledger.dto.AccountResponse;
import com.redledger.entity.Account;
import com.redledger.entity.AccountType;
import com.redledger.entity.User;
import com.redledger.repository.AccountRepository;
import com.redledger.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

	private static final Logger log = LoggerFactory.getLogger(AccountService.class);

	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
		this.accountRepository = accountRepository;
		this.userRepository = userRepository;
	}

	public Account createAccount(Long userId, AccountType accountType) {
		User owner = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		// TODO: [A4] — Predictable sequential account numbers (Phase 3)
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

	private String generateAccountNumber() {
		long next = accountRepository.findTopByOrderByIdDesc()
			.map(a -> Long.parseLong(a.getAccountNumber().substring(3)) + 1)
			.orElse(1L);
		return String.format("ACC%08d", next);
	}
}
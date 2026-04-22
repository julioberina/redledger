package com.redledger.controller;

import com.redledger.dto.AccountResponse;
import com.redledger.dto.BalanceResponse;
import com.redledger.dto.TransactionResponse;
import com.redledger.entity.Account;
import com.redledger.entity.AccountType;
import com.redledger.entity.User;
import com.redledger.service.AccountService;
import com.redledger.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;
	private final TransactionService transactionService;

	public AccountController(AccountService accountService, TransactionService transactionService) {
		this.accountService = accountService;
		this.transactionService = transactionService;
	}

	@PostMapping
	public ResponseEntity<AccountResponse> createAccount(@RequestParam AccountType accountType) {
		// TODO: [Phase 2.6] — replace with proper UserDetails once UserService is wired
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Long userId = ((User) auth.getPrincipal()).getId();

		Account created = accountService.createAccount(userId, accountType);
		return ResponseEntity.status(201).body(accountService.toAccountResponse(created));
	}

	@GetMapping
	public ResponseEntity<List<AccountResponse>> getMyAccounts() {
		User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Long userId = (Long) currentUser.getId();

		List<AccountResponse> accounts = accountService.getAccountsByUser(userId)
			.stream()
			.map(accountService::toAccountResponse)
			.collect(Collectors.toList());

		return ResponseEntity.ok(accounts);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AccountResponse> getAccount(@PathVariable Long id) {
		// TODO: [A1] — No ownership check here; IDOR vulnerability (Phase 3)
		return accountService.getAccount(id)
			.map(account -> ResponseEntity.ok(accountService.toAccountResponse(account)))
			.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}/balance")
	public ResponseEntity<BalanceResponse> getAccountBalance(@PathVariable Long id) {
		// TODO: [A1] — No ownership check here; IDOR vulnerability (Phase 3)
		return accountService.getAccount(id)
			.map(account -> ResponseEntity.ok(new BalanceResponse(id, account.getBalance())))
			.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}/transactions")
	public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(@PathVariable Long id) {
		// TODO: Add authorization check - IDOR vulnerability point (Phase 3)
		List<TransactionResponse> transactions = transactionService.getTransactionsByAccountId(id)
			.stream()
			.map(transactionService::toTransactionResponse)
			.collect(Collectors.toList());
		return ResponseEntity.ok(transactions);
	}
}
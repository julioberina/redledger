package com.redledger.controller;

import com.redledger.dto.AccountResponse;
import com.redledger.entity.Account;
import com.redledger.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {
        // TODO: Get authenticated user ID from SecurityContext
        // TODO: Return only accounts belonging to the authenticated user
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        // TODO: Add authorization check - potential IDOR vulnerability point
        return accountService.getAccount(id)
                .map(account -> ResponseEntity.ok(accountService.toAccountResponse(account)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<?> getAccountBalance(@PathVariable Long id) {
        // TODO: Add authorization check
        return accountService.getAccount(id)
                .map(account -> ResponseEntity.ok(
                        java.util.Map.of("accountId", account.getId(), "balance", account.getBalance())))
                .orElse(ResponseEntity.notFound().build());
    }
}

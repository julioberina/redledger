package com.redledger.service;

import com.redledger.dto.AccountResponse;
import com.redledger.entity.Account;
import com.redledger.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAccountsByOwnerId(Long ownerId) {
        // TODO: Add authorization check - users should only see their own accounts
        return accountRepository.findByOwnerId(ownerId);
    }

    public Optional<Account> getAccountById(Long id) {
        // TODO: Add authorization check - IDOR vulnerability point
        return accountRepository.findById(id);
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
}

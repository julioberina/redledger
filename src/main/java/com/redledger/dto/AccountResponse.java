package com.redledger.dto;

import com.redledger.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {

	private Long id;
	private String accountNumber;
	private AccountType accountType;
	private BigDecimal balance;
	private Long ownerId;
	private String ownerUsername;
	private LocalDateTime createdAt;

	public AccountResponse() {}

	// Getters and Setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getAccountNumber() { return accountNumber; }
	public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

	public AccountType getAccountType() { return accountType; }
	public void setAccountType(AccountType accountType) { this.accountType = accountType; }

	public BigDecimal getBalance() { return balance; }
	public void setBalance(BigDecimal balance) { this.balance = balance; }

	public Long getOwnerId() { return ownerId; }
	public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

	public String getOwnerUsername() { return ownerUsername; }
	public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

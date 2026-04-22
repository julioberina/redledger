package com.redledger.dto;

import com.redledger.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

	private Long id;
	private Long sourceAccountId;
	private String sourceAccountNumber;
	private Long destinationAccountId;
	private String destinationAccountNumber;
	private BigDecimal amount;
	private String description;
	private TransactionStatus status;
	private LocalDateTime createdAt;

	public TransactionResponse() {}

	// Getters and Setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Long getSourceAccountId() { return sourceAccountId; }
	public void setSourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; }

	public String getSourceAccountNumber() { return sourceAccountNumber; }
	public void setSourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; }

	public Long getDestinationAccountId() { return destinationAccountId; }
	public void setDestinationAccountId(Long destinationAccountId) { this.destinationAccountId = destinationAccountId; }

	public String getDestinationAccountNumber() { return destinationAccountNumber; }
	public void setDestinationAccountNumber(String destinationAccountNumber) { this.destinationAccountNumber = destinationAccountNumber; }

	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public TransactionStatus getStatus() { return status; }
	public void setStatus(TransactionStatus status) { this.status = status; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.redledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TransferRequest {

	@NotNull(message = "Source account ID is required")
	private Long sourceAccountId;

	@NotNull(message = "Destination account ID is required")
	private Long destinationAccountId;

	@NotNull(message = "Amount is required")
	/*
	 * VULN: [A8] — (3.A8.1) No amount validation. Negative amounts reverse money flow
	 * (effectively stealing from the destination account), zero amounts pollute the
	 * audit log. No maximum cap exists. CWE-20: Improper Input Validation.
	 */
	private BigDecimal amount;

	@NotBlank(message = "Description is required")
	private String description;

	public TransferRequest() {}

	public TransferRequest(Long sourceAccountId, Long destinationAccountId, BigDecimal amount, String description) {
		this.sourceAccountId = sourceAccountId;
		this.destinationAccountId = destinationAccountId;
		this.amount = amount;
		this.description = description;
	}

	public Long getSourceAccountId() { return sourceAccountId; }
	public void setSourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; }

	public Long getDestinationAccountId() { return destinationAccountId; }
	public void setDestinationAccountId(Long destinationAccountId) { this.destinationAccountId = destinationAccountId; }

	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
}

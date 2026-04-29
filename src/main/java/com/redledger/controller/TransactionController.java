package com.redledger.controller;

import com.redledger.dto.TransferRequest;
import com.redledger.dto.TransferResponse;
import com.redledger.dto.TransactionResponse;
import com.redledger.entity.TransactionStatus;
import com.redledger.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/*
	 * VULN: [A1] — (3.A1.4) No ownership check on sourceAccountId; any authenticated user can drain
	 * any account by supplying an arbitrary sourceAccountId in the request body (destructive IDOR)
	 */
	@PostMapping
	public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
		// VULN: [A1] — No ownership check on sourceAccountId; caller can transfer funds from any account (IDOR)
		TransferResponse response = transactionService.transfer(request);
		HttpStatus httpStatus = response.getStatus().equals(TransactionStatus.FAILED) ?
			HttpStatus.BAD_REQUEST :
			HttpStatus.CREATED;
		return ResponseEntity.status(httpStatus).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
		// VULN: [A1] — No ownership check; any authenticated user can retrieve any transaction by ID (IDOR)
		return transactionService.getTransactionById(id)
			.map(transactionService::toTransactionResponse)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	/*
	 * VULN: [A3] — (3.A3.2) GET /api/transactions/filter?status= passes unsanitised input
	 * directly to a raw JDBC query in TransactionService.filterTransactionsByStatus().
	 * Vulnerable to SQL injection via the status query parameter.
	 */
	@GetMapping("/filter")
	public ResponseEntity<?> filterByStatus(@RequestParam String status) {
		return ResponseEntity.ok(transactionService.filterTransactionsByStatus(status));
	}
}

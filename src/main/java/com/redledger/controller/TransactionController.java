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

	@PostMapping
	public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
		// TODO: Validate that source account belongs to authenticated user (Phase 3 IDOR stub)
		TransferResponse response = transactionService.transfer(request);
		HttpStatus httpStatus = response.getStatus().equals(TransactionStatus.FAILED) ?
			HttpStatus.BAD_REQUEST :
			HttpStatus.CREATED;
		return ResponseEntity.status(httpStatus).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
		// TODO: Add authorization check - IDOR vulnerability point (Phase 3)
		return transactionService.getTransactionById(id)
			.map(transactionService::toTransactionResponse)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}
}

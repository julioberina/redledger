package com.redledger.controller;

import com.redledger.dto.TransferRequest;
import com.redledger.dto.TransferResponse;
import com.redledger.dto.TransactionResponse;
import com.redledger.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        // TODO: Validate that source account belongs to authenticated user
        TransferResponse response = transactionService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(@PathVariable Long accountId) {
        // TODO: Add authorization check - potential IDOR vulnerability point
        List<TransactionResponse> transactions = transactionService.getTransactionsByAccountId(accountId)
                .stream()
                .map(transactionService::toTransactionResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        // TODO: Add authorization check - potential IDOR vulnerability point
        return transactionService.getTransactionById(id)
                .map(transactionService::toTransactionResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

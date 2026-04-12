package com.redledger.service;

import com.redledger.dto.TransferRequest;
import com.redledger.dto.TransferResponse;
import com.redledger.dto.TransactionResponse;
import com.redledger.entity.Transaction;
import com.redledger.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransferResponse transfer(TransferRequest request) {
        // TODO: Implement transfer logic
        // 1. Validate source account belongs to authenticated user
        // 2. Validate destination account exists
        // 3. Check sufficient balance
        // 4. Perform transfer (debit source, credit destination)
        // 5. Create transaction record
        throw new UnsupportedOperationException("Transfer not yet implemented");
    }

    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        // TODO: Add authorization check
        return transactionRepository.findBySourceAccountIdOrDestinationAccountId(accountId, accountId);
    }

    public Optional<Transaction> getTransactionById(Long id) {
        // TODO: Add authorization check - IDOR vulnerability point
        return transactionRepository.findById(id);
    }

    public TransactionResponse toTransactionResponse(Transaction tx) {
        TransactionResponse response = new TransactionResponse();
        response.setId(tx.getId());
        response.setSourceAccountId(tx.getSourceAccount().getId());
        response.setSourceAccountNumber(tx.getSourceAccount().getAccountNumber());
        response.setDestinationAccountId(tx.getDestinationAccount().getId());
        response.setDestinationAccountNumber(tx.getDestinationAccount().getAccountNumber());
        response.setAmount(tx.getAmount());
        response.setDescription(tx.getDescription());
        response.setStatus(tx.getStatus());
        response.setCreatedAt(tx.getCreatedAt());
        return response;
    }
}

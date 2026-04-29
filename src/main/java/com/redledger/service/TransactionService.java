package com.redledger.service;

import com.redledger.dto.TransferRequest;
import com.redledger.dto.TransferResponse;
import com.redledger.dto.TransactionResponse;
import com.redledger.entity.Account;
import com.redledger.entity.Transaction;
import com.redledger.entity.TransactionStatus;
import com.redledger.repository.AccountRepository;
import com.redledger.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionService {
	private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

	private final TransactionRepository transactionRepository;
	private final AccountRepository accountRepository;
	private final JdbcTemplate jdbcTemplate;

	public TransactionService(TransactionRepository transactionRepository,
							  AccountRepository accountRepository,
							  JdbcTemplate jdbcTemplate) {
		this.transactionRepository = transactionRepository;
		this.accountRepository = accountRepository;
		this.jdbcTemplate = jdbcTemplate;
	}

	/*
	 * VULN: [A1] — (3.A1.4) sourceAccountId is caller-supplied with no ownership validation against the
	 * authenticated principal. Any authenticated user can initiate a transfer debiting an account they do
	 * not own. Unlike the read-only IDOR in 3.A1.2, this is a destructive write operation — funds can be
	 * moved out of a victim's account without their knowledge.
	 */
	@Transactional
	public TransferResponse transfer(TransferRequest request) {
		// VULN: [A1] — sourceAccountId taken from request body, not validated against authenticated user; attacker can initiate transfers from any account (IDOR)
		Account source = accountRepository.findById(request.getSourceAccountId())
			.orElseThrow(() -> new IllegalArgumentException(
				"Source account not found: " + request.getSourceAccountId()));

		Account destination = accountRepository.findById(request.getDestinationAccountId())
			.orElseThrow(() -> new IllegalArgumentException(
				"Destination account not found: " + request.getDestinationAccountId()));

		if (source.getId().equals(destination.getId()))
			throw new IllegalArgumentException("Source and destination accounts must differ");

		if (source.getBalance().compareTo(request.getAmount()) < 0) {
			log.warn("Insufficient balance on account {} for transfer of {}",
				source.getId(), request.getAmount());

			Transaction failed = new Transaction(source, destination,
				request.getAmount(), request.getDescription(), TransactionStatus.FAILED);

			transactionRepository.save(failed);

			TransferResponse response = new TransferResponse();
			response.setStatus(TransactionStatus.FAILED);

			return response;
		}

		source.setBalance(source.getBalance().subtract(request.getAmount()));
		destination.setBalance(destination.getBalance().add(request.getAmount()));

		accountRepository.save(source);
		accountRepository.save(destination);

		Transaction tx = new Transaction(source, destination,
			request.getAmount(), request.getDescription(), TransactionStatus.COMPLETED);

		transactionRepository.save(tx);

		// VULN: [A2] — (3.A2.3) Full account numbers logged on every transfer; sensitive financial data exposed in log files
		log.debug("Transfer of {} from account {} ({}) to account {} ({}) completed",
			request.getAmount(), source.getId(), source.getAccountNumber(),
			destination.getId(), destination.getAccountNumber());

		TransferResponse response = new TransferResponse();
		response.setTransactionId(tx.getId());
		response.setStatus(TransactionStatus.COMPLETED);

		return response;
	}

	public List<Transaction> getTransactionsByAccountId(Long accountId) {
		// VULN: [A1] — No ownership check; any authenticated user can list transactions for any account ID (IDOR)
		return transactionRepository.findBySourceAccountIdOrDestinationAccountId(accountId, accountId);
	}

	public Optional<Transaction> getTransactionById(Long id) {
		// VULN: [A1] — No ownership check; any authenticated user can fetch any transaction by ID (IDOR)
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

	/*
	 * VULN: [A3] — (3.A3.2) Raw user input concatenated directly into SQL query string.
	 * The `status` parameter is unsanitised — attacker can inject arbitrary SQL via the filter
	 * endpoint, e.g. `COMPLETED' OR '1'='1` dumps all transactions regardless of ownership.
	 */
	public List<java.util.Map<String, Object>> filterTransactionsByStatus(String status) {
		String query = "SELECT * FROM transactions WHERE status = '" + status + "'";
		return jdbcTemplate.queryForList(query);
	}
}

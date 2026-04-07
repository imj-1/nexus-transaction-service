package com.nexus.banking.transaction_service.common.service;

import com.nexus.banking.transaction_service.client.AccountClient;
import com.nexus.banking.transaction_service.client.dto.AccountResponse;
import com.nexus.banking.transaction_service.common.entity.Transaction;
import com.nexus.banking.transaction_service.common.enums.TransactionStatus;
import com.nexus.banking.transaction_service.common.enums.TransactionType;
import com.nexus.banking.transaction_service.common.exception.InsufficientFundsException;
import com.nexus.banking.transaction_service.common.exception.InvalidTransactionException;
import com.nexus.banking.transaction_service.common.exception.ResourceNotFoundException;
import com.nexus.banking.transaction_service.common.messaging.TransactionEventPublisher;
import com.nexus.banking.transaction_service.common.repository.TransactionRepository;
import com.nexus.banking.transaction_service.v1.dto.request.DepositRequest;
import com.nexus.banking.transaction_service.v1.dto.request.TransferRequest;
import com.nexus.banking.transaction_service.v1.dto.request.WithdrawalRequest;
import com.nexus.banking.transaction_service.v1.dto.response.TransactionDTO;
import com.nexus.banking.transaction_service.v1.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Core transaction orchestration.
 * <p>
 * DISTRIBUTED CONSISTENCY NOTE
 * Balance updates are synchronous REST calls to account-service.
 * If debit succeeds but credit fails, the transaction is marked FAILED
 * and the debit is NOT automatically reversed. For production-grade
 * guarantees, implement the Saga pattern with compensating transactions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final TransactionEventPublisher eventPublisher;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionDTO transfer(TransferRequest request, String userId) {
        if (request.fromAccountId()
                   .equals(request.toAccountId())) {
            throw new InvalidTransactionException("Source and destination accounts must be different.");
        }

        AccountResponse from = accountClient.getAccountById(request.fromAccountId());
        AccountResponse to = accountClient.getAccountById(request.toAccountId());

        validateSufficientFunds(from, request.amount());

        Transaction txn = transactionRepository.save(Transaction.builder()
                                                                .transactionReference(generateReference())
                                                                .fromAccountId(from.id())
                                                                .toAccountId(to.id())
                                                                .amount(request.amount())
                                                                .type(TransactionType.TRANSFER)
                                                                .status(TransactionStatus.PENDING)
                                                                .initiatedBy(userId)
                                                                .description(request.description())
                                                                .build());

        try {
            accountClient.debitAccount(from.id(), request.amount());
            accountClient.creditAccount(to.id(), request.amount());
            txn.setStatus(TransactionStatus.COMPLETED);
            log.info(
                    "Transfer COMPLETED ref={} from={} to={} amount={}",
                    txn.getTransactionReference(),
                    from.id(),
                    to.id(),
                    request.amount()
                    );
        } catch (Exception ex) {
            txn.setStatus(TransactionStatus.FAILED);
            txn.setFailureReason(ex.getMessage());
            log.error("Transfer FAILED ref={} reason={}", txn.getTransactionReference(), ex.getMessage());
        }

        Transaction saved = transactionRepository.save(txn);
        eventPublisher.publish(saved);
        return transactionMapper.toDto(saved);
    }

    @Override
    @Transactional
    public TransactionDTO deposit(DepositRequest request, String userId) {
        AccountResponse to = accountClient.getAccountById(request.toAccountId());

        Transaction txn = transactionRepository.save(Transaction.builder()
                                                                .transactionReference(generateReference())
                                                                .toAccountId(to.id())
                                                                .amount(request.amount())
                                                                .type(TransactionType.DEPOSIT)
                                                                .status(TransactionStatus.PENDING)
                                                                .initiatedBy(userId)
                                                                .description(request.description())
                                                                .build());

        try {
            accountClient.creditAccount(to.id(), request.amount());
            txn.setStatus(TransactionStatus.COMPLETED);
            log.info(
                    "Deposit COMPLETED ref={} to={} amount={}",
                    txn.getTransactionReference(),
                    to.id(),
                    request.amount()
                    );
        } catch (Exception ex) {
            txn.setStatus(TransactionStatus.FAILED);
            txn.setFailureReason(ex.getMessage());
            log.error("Deposit FAILED ref={} reason={}", txn.getTransactionReference(), ex.getMessage());
        }

        Transaction saved = transactionRepository.save(txn);
        eventPublisher.publish(saved);
        return transactionMapper.toDto(saved);
    }

    @Override
    @Transactional
    public TransactionDTO withdraw(WithdrawalRequest request, String userId) {
        AccountResponse from = accountClient.getAccountById(request.fromAccountId());
        validateSufficientFunds(from, request.amount());

        Transaction txn = transactionRepository.save(Transaction.builder()
                                                                .transactionReference(generateReference())
                                                                .fromAccountId(from.id())
                                                                .amount(request.amount())
                                                                .type(TransactionType.WITHDRAWAL)
                                                                .status(TransactionStatus.PENDING)
                                                                .initiatedBy(userId)
                                                                .description(request.description())
                                                                .build());

        try {
            accountClient.debitAccount(from.id(), request.amount());
            txn.setStatus(TransactionStatus.COMPLETED);
            log.info(
                    "Withdrawal COMPLETED ref={} from={} amount={}",
                    txn.getTransactionReference(),
                    from.id(),
                    request.amount()
                    );
        } catch (Exception ex) {
            txn.setStatus(TransactionStatus.FAILED);
            txn.setFailureReason(ex.getMessage());
            log.error("Withdrawal FAILED ref={} reason={}", txn.getTransactionReference(), ex.getMessage());
        }

        Transaction saved = transactionRepository.save(txn);
        eventPublisher.publish(saved);
        return transactionMapper.toDto(saved);
    }

    @Override
    @Cacheable(value = "transactions", key = "#id")
    public TransactionDTO getTransactionById(Long id) {
        return transactionRepository.findById(id)
                                    .map(transactionMapper::toDto)
                                    .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: "
                                                                                             + id));
    }

    @Override
    @Cacheable(value = "transactions", key = "#reference")
    public TransactionDTO getTransactionByReference(String reference) {
        return transactionRepository.findByTransactionReference(reference)
                                    .map(transactionMapper::toDto)
                                    .orElseThrow(() -> new ResourceNotFoundException(
                                            "Transaction not found with reference: " + reference));
    }

    @Override
    public Page<TransactionDTO> getTransactionsByAccountId(Long accountId, Pageable pageable) {
        return transactionRepository.findByAccountId(accountId, pageable)
                                    .map(transactionMapper::toDto);
    }

    @Override
    public Page<TransactionDTO> getTransactionsByUserId(String userId, Pageable pageable) {
        return transactionRepository.findByInitiatedByOrderByCreatedAtDesc(userId, pageable)
                                    .map(transactionMapper::toDto);
    }

    private void validateSufficientFunds(AccountResponse account, BigDecimal amount) {
        if (account.balance()
                   .compareTo(amount) < 0) {
            throw new InsufficientFundsException(String.format(
                    "Insufficient funds in account %s. Available: %s, Required: %s",
                    account.accountNumber(),
                    account.balance(),
                    amount
                                                              ));
        }
    }

    private String generateReference() {
        return "TXN" + Instant.now()
                              .toEpochMilli();
    }
}
package com.nexus.banking.transaction_service.v1.dto.response;

import com.nexus.banking.transaction_service.common.enums.TransactionStatus;
import com.nexus.banking.transaction_service.common.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDTO(
        Long id,
        String transactionReference,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        String initiatedBy,
        String description,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        BigDecimal balanceAfter
) {}
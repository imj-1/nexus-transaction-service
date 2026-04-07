package com.nexus.banking.transaction_service.common.messaging.event;

import com.nexus.banking.transaction_service.common.enums.TransactionStatus;
import com.nexus.banking.transaction_service.common.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Published to Kafka topic "transaction.events" after every transaction attempt.
 * <p>
 * Potential consumers:
 * - account-service  : audit log
 * - notification-service (future): push/email alerts
 * - fraud-detection  (future): real-time pattern analysis
 */
public record TransactionEvent(
        String transactionReference,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        String initiatedBy,
        String failureReason,
        LocalDateTime occurredAt
) {}
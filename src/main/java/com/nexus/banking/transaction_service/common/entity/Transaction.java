package com.nexus.banking.transaction_service.common.entity;

import com.nexus.banking.transaction_service.common.enums.TransactionStatus;
import com.nexus.banking.transaction_service.common.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transactions", indexes = {
        @Index(name = "idx_txn_from_account", columnList = "fromAccountId"),
        @Index(name = "idx_txn_to_account", columnList = "toAccountId"),
        @Index(name = "idx_txn_initiated_by", columnList = "initiatedBy"),
        @Index(name = "idx_txn_reference", columnList = "transactionReference", unique = true),
        @Index(name = "idx_txn_status", columnList = "status"),
        @Index(name = "idx_txn_created_at", columnList = "createdAt")
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique human-readable reference e.g. TXN1718123456789.
     */
    @Column(nullable = false, unique = true, length = 32)
    private String transactionReference;

    /**
     * Source account — null for DEPOSIT transactions.
     */
    @Column
    private Long fromAccountId;

    /**
     * Destination account — null for WITHDRAWAL transactions.
     */
    @Column
    private Long toAccountId;

    /**
     * Stored with up to 4 decimal places for currency precision.
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    /**
     * Keycloak subject (sub claim) of the user who initiated the transaction.
     */
    @Column(nullable = false)
    private String initiatedBy;

    @Column(length = 500)
    private String description;

    /**
     * Populated only when status = FAILED.
     */
    @Column(length = 1000)
    private String failureReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

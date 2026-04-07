package com.nexus.banking.transaction_service.common.enums;

public enum TransactionStatus {
    /**
     * Record created; balance updates not yet applied.
     */
    PENDING,

    /**
     * All balance updates applied successfully.
     */
    COMPLETED,

    /**
     * An error occurred; see failureReason field.
     */
    FAILED,

    /**
     * Transaction reversed (reserved for future compensating logic).
     */
    REVERSED
}
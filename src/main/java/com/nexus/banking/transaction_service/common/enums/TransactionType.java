package com.nexus.banking.transaction_service.common.enums;

public enum TransactionType {
    /**
     * Money moved between two accounts within Nexus Banking.
     */
    TRANSFER,

    /**
     * External funds added to an account.
     */
    DEPOSIT,

    /**
     * Funds removed from an account to an external destination.
     */
    WITHDRAWAL
}

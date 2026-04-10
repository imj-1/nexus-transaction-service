package com.nexus.banking.transaction_service.client.dto;

import java.math.BigDecimal;

/**
 * Mirror of AccountDTO from account-service.
 * Fields derived from the Account entity:
 * Long id | String accountNumber | BigDecimal balance | String userId
 */
public record AccountResponse(
        Long id,
        String accountNumber,
        String accountType,
        BigDecimal balance,
        BigDecimal availableBalance,
        String userId
) {}
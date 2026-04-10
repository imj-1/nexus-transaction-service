package com.nexus.banking.transaction_service.v1.dto.response;

import java.math.BigDecimal;

public record AccountMonthlySummaryDTO(
        String month,
        BigDecimal totalDeposits,
        BigDecimal totalWithdrawals
) {}
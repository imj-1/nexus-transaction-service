package com.nexus.banking.transaction_service.client.dto;

import java.math.BigDecimal;

public record AccountBalanceResponse(BigDecimal newBalance) {}
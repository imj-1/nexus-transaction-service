package com.nexus.banking.transaction_service.v1.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "Source account ID is required") Long fromAccountId,

        @NotNull(message = "Destination account ID is required") Long toAccountId,

        @NotNull(message = "Amount is required") @Positive(
                message = "Amount must be greater than zero"
        ) BigDecimal amount,

        @Size(max = 500, message = "Description cannot exceed 500 characters") String description
) {}

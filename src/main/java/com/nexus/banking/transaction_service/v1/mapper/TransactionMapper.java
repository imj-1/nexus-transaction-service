package com.nexus.banking.transaction_service.v1.mapper;

import com.nexus.banking.transaction_service.common.entity.Transaction;
import com.nexus.banking.transaction_service.v1.dto.response.TransactionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

/**
 * MapStruct maps all fields by name.
 * balanceAfter is excluded from auto-mapping — it's resolved per-account
 * perspective via toDtoWithPerspective().
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "balanceAfter", ignore = true)
    TransactionDTO toDto(Transaction transaction);

    /**
     * Resolves the correct balance snapshot based on whether the queried
     * account was the sender (fromBalanceAfter) or receiver (toBalanceAfter).
     */
    default TransactionDTO toDtoWithPerspective(Transaction t, Long accountId) {
        BigDecimal balanceAfter =
                accountId.equals(t.getToAccountId()) ? t.getToBalanceAfter() : t.getFromBalanceAfter();

        return new TransactionDTO(
                t.getId(),
                t.getTransactionReference(),
                t.getFromAccountId(),
                t.getToAccountId(),
                t.getAmount(),
                t.getType(),
                t.getStatus(),
                t.getInitiatedBy(),
                t.getDescription(),
                t.getFailureReason(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                balanceAfter
        );
    }
}
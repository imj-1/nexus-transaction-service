package com.nexus.banking.transaction_service.v1.mapper;

import com.nexus.banking.transaction_service.common.entity.Transaction;
import com.nexus.banking.transaction_service.v1.dto.response.TransactionDTO;
import org.mapstruct.Mapper;

/**
 * MapStruct maps all fields by name — Transaction entity fields align
 * exactly with TransactionDTO record components, so no @Mapping needed.
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionDTO toDto(Transaction transaction);
}
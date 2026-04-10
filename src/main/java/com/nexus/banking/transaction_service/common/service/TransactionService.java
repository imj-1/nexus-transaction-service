package com.nexus.banking.transaction_service.common.service;

import com.nexus.banking.transaction_service.v1.dto.request.DepositRequest;
import com.nexus.banking.transaction_service.v1.dto.request.TransferRequest;
import com.nexus.banking.transaction_service.v1.dto.request.WithdrawalRequest;
import com.nexus.banking.transaction_service.v1.dto.response.AccountMonthlySummaryDTO;
import com.nexus.banking.transaction_service.v1.dto.response.TransactionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {

    TransactionDTO transfer(TransferRequest request, String userId);

    TransactionDTO deposit(DepositRequest request, String userId);

    TransactionDTO withdraw(WithdrawalRequest request, String userId);

    TransactionDTO getTransactionById(Long id);

    TransactionDTO getTransactionByReference(String reference);

    Page<TransactionDTO> getTransactionsByAccountId(Long accountId, Pageable pageable);

    Page<TransactionDTO> getTransactionsByUserId(String userId, Pageable pageable);

    AccountMonthlySummaryDTO getMonthlySummary(Long accountId);
}
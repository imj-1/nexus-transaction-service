package com.nexus.banking.transaction_service.v1.controller;

import com.nexus.banking.transaction_service.common.service.TransactionService;
import com.nexus.banking.transaction_service.v1.dto.request.DepositRequest;
import com.nexus.banking.transaction_service.v1.dto.request.TransferRequest;
import com.nexus.banking.transaction_service.v1.dto.request.WithdrawalRequest;
import com.nexus.banking.transaction_service.v1.dto.response.AccountMonthlySummaryDTO;
import com.nexus.banking.transaction_service.v1.dto.response.TransactionDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // ── Mutations ─────────────────────────────────────────────────────────

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDTO> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal Jwt jwt
                                                  ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(transactionService.transfer(request, jwt.getSubject()));
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionDTO> deposit(
            @Valid @RequestBody DepositRequest request,
            @AuthenticationPrincipal Jwt jwt
                                                 ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(transactionService.deposit(request, jwt.getSubject()));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionDTO> withdraw(
            @Valid @RequestBody WithdrawalRequest request,
            @AuthenticationPrincipal Jwt jwt
                                                  ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(transactionService.withdraw(request, jwt.getSubject()));
    }

    // ── Queries ───────────────────────────────────────────────────────────

    /**
     * Current user's own transactions — userId resolved from JWT, no path param needed.
     */
    @GetMapping("/me")
    public ResponseEntity<Page<TransactionDTO>> getMyTransactions(
            @AuthenticationPrincipal Jwt jwt, @PageableDefault(
                    size = 20, sort = "createdAt"
            ) Pageable pageable
                                                                 ) {
        return ResponseEntity.ok(transactionService.getTransactionsByUserId(jwt.getSubject(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<TransactionDTO> getByReference(
            @PathVariable String reference
                                                        ) {
        return ResponseEntity.ok(transactionService.getTransactionByReference(reference));
    }

    /**
     * All transactions for a given account (admin-facing or ownership-verified).
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<TransactionDTO>> getByAccountId(
            @PathVariable Long accountId, @PageableDefault(
                    size = 20, sort = "createdAt"
            ) Pageable pageable
                                                              ) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccountId(accountId, pageable));
    }

    /**
     * All transactions for a given user — admin-facing endpoint.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TransactionDTO>> getByUserId(
            @PathVariable String userId, @PageableDefault(
                    size = 20, sort = "createdAt"
            ) Pageable pageable
                                                           ) {
        return ResponseEntity.ok(transactionService.getTransactionsByUserId(userId, pageable));
    }

    /**
     * Monthly deposit / withdrawal summary for a given account.
     * Consumed by the API Gateway aggregation layer.
     */
    @GetMapping("/account/{accountId}/summary")
    public ResponseEntity<AccountMonthlySummaryDTO> getAccountMonthlySummary(
            @PathVariable Long accountId
                                                                            ) {
        return ResponseEntity.ok(transactionService.getMonthlySummary(accountId));
    }
}

package com.nexus.banking.transaction_service.client;

import com.nexus.banking.transaction_service.client.dto.AccountResponse;
import com.nexus.banking.transaction_service.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountClient {

    private final WebClient accountWebClient;

    /**
     * GET /api/v1/accounts/{id} — existing endpoint on account-service.
     */
    public AccountResponse getAccountById(Long accountId) {
        log.debug("Fetching account id={}", accountId);
        return accountWebClient.get()
                               .uri("/api/v1/accounts/{id}", accountId)
                               .retrieve()
                               .onStatus(
                                       status -> status.value() == 404,
                                       response -> Mono.error(new ResourceNotFoundException(
                                               "Account not found with id: " + accountId))
                                        )
                               .onStatus(HttpStatusCode::is4xxClientError, response -> response.createException())
                               .onStatus(HttpStatusCode::is5xxServerError, response -> response.createException())
                               .bodyToMono(AccountResponse.class)
                               .block();
    }

    /**
     * PATCH /internal/v1/accounts/{id}/debit — new internal endpoint.
     * See REQUIRED_CHANGES.md for the account-service implementation.
     */
    public void debitAccount(Long accountId, BigDecimal amount) {
        log.debug("Debiting account id={}, amount={}", accountId, amount);
        accountWebClient.patch()
                        .uri("/internal/v1/accounts/{id}/debit", accountId)
                        .bodyValue(Map.of("amount", amount))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, response -> response.createException())
                        .bodyToMono(Void.class)
                        .block();
    }

    /**
     * PATCH /internal/v1/accounts/{id}/credit — new internal endpoint.
     * See REQUIRED_CHANGES.md for the account-service implementation.
     */
    public void creditAccount(Long accountId, BigDecimal amount) {
        log.debug("Crediting account id={}, amount={}", accountId, amount);
        accountWebClient.patch()
                        .uri("/internal/v1/accounts/{id}/credit", accountId)
                        .bodyValue(Map.of("amount", amount))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, response -> response.createException())
                        .bodyToMono(Void.class)
                        .block();
    }
}
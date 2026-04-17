package com.nexus.banking.transaction_service.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    /**
     * @LoadBalanced enables Spring Cloud LoadBalancer to resolve service names
     * registered in Eureka. Uses http:// (not lb://) — that prefix is for
     * Gateway's RouteLocator only. WebClient uses http://service-name.
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient accountWebClient(@LoadBalanced WebClient.Builder builder) {
        return builder.baseUrl("http://account-service")
                      .defaultHeader("X-Internal-Service", "transaction-service")
                      .filter(jwtForwardingFilter())
                      .build();
    }

    /**
     * Pulls the JWT from Spring Security's SecurityContextHolder and attaches
     * it as a Bearer token on every outbound request to account-service.
     * <p>
     * This is necessary because account-service's public endpoints (e.g.
     * GET /api/v1/accounts/{id}) require a valid JWT. The internal debit/credit
     * endpoints under /internal/** are excluded from this requirement but the
     * header is harmless there.
     */
    private ExchangeFilterFunction jwtForwardingFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            var auth = SecurityContextHolder.getContext()
                                            .getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                String token = jwtAuth.getToken()
                                      .getTokenValue();
                ClientRequest mutated = ClientRequest.from(request)
                                                     .header("Authorization", "Bearer " + token)
                                                     .build();
                return Mono.just(mutated);
            }
            return Mono.just(request);
        });
    }
}
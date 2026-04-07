package com.nexus.banking.transaction_service.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

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
                      .build();
    }
}
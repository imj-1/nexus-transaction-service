package com.nexus.banking.transaction_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/actuator/health", "/actuator/info"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                   .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                   .authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_PATHS)
                                                      .permitAll()
                                                      .requestMatchers(HttpMethod.GET, "/api/v1/transactions/**")
                                                      .hasAuthority("SCOPE_transactions:read")
                                                      .requestMatchers(HttpMethod.POST, "/api/v1/transactions/**")
                                                      .hasAuthority("SCOPE_transactions:write")
                                                      .anyRequest()
                                                      .authenticated())
                   .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                           jwtAuthenticationConverter())))
                   .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("SCOPE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtConverter;
    }
}
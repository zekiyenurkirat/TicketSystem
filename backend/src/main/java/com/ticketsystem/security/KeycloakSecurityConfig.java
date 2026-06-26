package com.ticketsystem.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Keycloak JWT doğrulaması için ayrı SecurityFilterChain.
 * Yalnızca /api/v1/keycloak/** path'ini yakalar; mevcut custom JWT akışına dokunmaz.
 * app.keycloak.enabled=false (test profili) iken bu konfigürasyon tamamen devre dışıdır.
 */
@Configuration
@ConditionalOnProperty(name = "app.keycloak.enabled", havingValue = "true")
public class KeycloakSecurityConfig {

    @Value("${app.keycloak.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    @Order(1)
    public SecurityFilterChain keycloakSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/keycloak/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build())
                    .jwtAuthenticationConverter(new KeycloakJwtConverter())
                )
            );
        return http.build();
    }
}

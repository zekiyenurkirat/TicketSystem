package com.ticketsystem;

import com.ticketsystem.security.KeycloakJwtConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakJwtConverterTest {

    private KeycloakJwtConverter converter;

    @BeforeEach
    void setUp() {
        converter = new KeycloakJwtConverter();
    }

    @Test
    void convert_extractsManagerRole() {
        Jwt jwt = buildJwt(Map.of("roles", List.of("MANAGER", "default-roles-ticketsystem")), "manager@example.com");

        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertThat(auth.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_MANAGER");
    }

    @Test
    void convert_filtersOutNonAppRoles() {
        Jwt jwt = buildJwt(Map.of("roles", List.of("AGENT", "offline_access", "uma_authorization")), "agent@example.com");

        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertThat(auth.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_AGENT");
    }

    @Test
    void convert_usesEmailAsPrincipalName() {
        Jwt jwt = buildJwt(Map.of("roles", List.of("CUSTOMER")), "customer@example.com");

        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertThat(auth.getName()).isEqualTo("customer@example.com");
    }

    @Test
    void convert_returnsEmptyAuthoritiesWhenNoRealmAccess() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-id")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertThat(auth.getAuthorities()).isEmpty();
    }

    private Jwt buildJwt(Map<String, Object> realmAccessRoles, String email) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("user-" + email)
                .claim("email", email)
                .claim("realm_access", realmAccessRoles)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}

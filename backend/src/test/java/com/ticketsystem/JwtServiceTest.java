package com.ticketsystem;

import com.ticketsystem.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha");
        ReflectionTestUtils.setField(jwtService, "expiration", 3_600_000L);
    }

    private UserDetails userDetails(String email) {
        return User.withUsername(email)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtService.generateToken(userDetails("user@test.com"));
        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_returnsCorrectEmail() {
        String email = "alice@test.com";
        String token = jwtService.generateToken(userDetails(email));
        assertThat(jwtService.extractUsername(token)).isEqualTo(email);
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        UserDetails ud = userDetails("bob@test.com");
        String token = jwtService.generateToken(ud);
        assertThat(jwtService.validateToken(token, ud)).isTrue();
    }

    @Test
    void validateToken_returnsFalseForWrongUser() {
        String token = jwtService.generateToken(userDetails("alice@test.com"));
        UserDetails other = userDetails("mallory@test.com");
        assertThat(jwtService.validateToken(token, other)).isFalse();
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1L);
        UserDetails ud = userDetails("expired@test.com");
        String token = jwtService.generateToken(ud);
        assertThat(jwtService.validateToken(token, ud)).isFalse();
    }

    @Test
    void generateToken_withExtraClaims_embedsClaims() {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("role", "MANAGER");
        claims.put("userId", 42L);
        UserDetails ud = userDetails("manager@test.com");
        String token = jwtService.generateToken(claims, ud);

        String role = jwtService.extractClaim(token, c -> c.get("role", String.class));
        Long userId = jwtService.extractClaim(token, c -> c.get("userId", Long.class));

        assertThat(role).isEqualTo("MANAGER");
        assertThat(userId).isEqualTo(42L);
    }

    @Test
    void extractUsername_throwsOnTamperedToken() {
        String token = jwtService.generateToken(userDetails("test@test.com"));
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThatThrownBy(() -> jwtService.extractUsername(tampered))
                .isInstanceOf(Exception.class);
    }
}

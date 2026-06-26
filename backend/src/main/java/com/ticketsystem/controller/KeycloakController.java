package com.ticketsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Keycloak JWT ile kimlik doğrulanmış kullanıcılar için demo endpoint'i.
 * Yalnızca /api/v1/keycloak/** path'i KeycloakSecurityConfig tarafından korunur.
 */
@RestController
@RequestMapping("/api/v1/keycloak")
public class KeycloakController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }
        List<String> authorities = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("sub", jwt.getSubject());
        info.put("email", jwt.getClaimAsString("email"));
        info.put("preferred_username", jwt.getClaimAsString("preferred_username"));
        info.put("authorities", authorities);
        return ResponseEntity.ok(info);
    }
}

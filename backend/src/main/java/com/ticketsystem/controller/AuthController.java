package com.ticketsystem.controller;

import com.ticketsystem.core.exception.BusinessRuleException;
import com.ticketsystem.core.response.ApiResponse;
import com.ticketsystem.dto.request.EnableTotpRequest;
import com.ticketsystem.dto.request.LoginRequest;
import com.ticketsystem.dto.request.VerifyTotpRequest;
import com.ticketsystem.dto.response.AuthResponse;
import com.ticketsystem.dto.response.TotpSetupResponse;
import com.ticketsystem.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Kimlik doğrulama HTTP isteklerini karşılar. */
@Tag(name = "Kimlik Doğrulama", description = "Kullanıcı girişi, JWT token üretimi ve 2FA yönetimi")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @Operation(summary = "Kullanıcı girişi",
               description = "2FA kapalı: JWT token döner. 2FA açık: requiresTwoFactor=true ve challengeToken döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Giriş başarılı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Email veya şifre hatalı.")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody @Valid LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        String message = authResponse.isRequiresTwoFactor()
                ? "Parola doğrulandı. Lütfen Google Authenticator kodunu girin."
                : "Giriş başarılı.";
        return ResponseEntity.ok(ApiResponse.success(authResponse, message));
    }

    // ── 2FA Setup (JWT gerektirir) ────────────────────────────────────────────

    @Operation(summary = "2FA kurulumu başlat",
               description = "TOTP secret üretir. Dönen otpAuthUri Google Authenticator'a eklenir. JWT gerektirir.")
    @PostMapping("/2fa/setup")
    public ResponseEntity<ApiResponse<TotpSetupResponse>> setupTotp(
            @AuthenticationPrincipal UserDetails userDetails) {
        requireAuthenticated(userDetails);
        TotpSetupResponse response = authService.setupTotp(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "2FA kurulum bilgileri üretildi."));
    }

    // ── 2FA Enable (JWT gerektirir) ───────────────────────────────────────────

    @Operation(summary = "2FA'yı aktif et",
               description = "Google Authenticator'dan alınan kodu doğrular ve 2FA'yı kalıcı olarak açar. JWT gerektirir.")
    @PostMapping("/2fa/enable")
    public ResponseEntity<ApiResponse<Void>> enableTotp(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid EnableTotpRequest request) {
        requireAuthenticated(userDetails);
        authService.enableTotp(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "2FA başarıyla etkinleştirildi."));
    }

    // ── 2FA Verify (permitAll — login ikinci adımı) ───────────────────────────

    @Operation(summary = "TOTP kodunu doğrula",
               description = "Login'den dönen challengeToken + Google Authenticator kodu ile tam JWT alınır.")
    @PostMapping("/2fa/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyTotp(
            @RequestBody @Valid VerifyTotpRequest request) {
        AuthResponse authResponse = authService.verifyTotp(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "2FA doğrulaması başarılı."));
    }

    // ── yardımcı ─────────────────────────────────────────────────────────────

    private void requireAuthenticated(UserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessRuleException("Bu işlem için oturum açmanız gerekmektedir.");
        }
    }
}

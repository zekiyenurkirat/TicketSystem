package com.ticketsystem.controller;

import com.ticketsystem.core.response.ApiResponse;
import com.ticketsystem.dto.request.LoginRequest;
import com.ticketsystem.dto.response.AuthResponse;
import com.ticketsystem.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Kimlik doğrulama HTTP isteklerini karşılar. */
@Tag(name = "Kimlik Doğrulama", description = "Kullanıcı girişi ve JWT token üretimi")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Kullanıcı girişi yapar ve JWT token döner. */
    @Operation(summary = "Kullanıcı girişi yapar",
               description = "Email ve şifre ile giriş yapılır; başarılı girişte JWT token döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Giriş başarılı, JWT token üretildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validasyon hatası veya geçersiz istek."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Email veya şifre hatalı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody @Valid LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Giriş başarılı."));
    }
}

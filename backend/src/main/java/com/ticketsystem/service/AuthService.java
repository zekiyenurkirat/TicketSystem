package com.ticketsystem.service;

import com.ticketsystem.dto.request.EnableTotpRequest;
import com.ticketsystem.dto.request.LoginRequest;
import com.ticketsystem.dto.request.VerifyTotpRequest;
import com.ticketsystem.dto.response.AuthResponse;
import com.ticketsystem.dto.response.TotpSetupResponse;

/** Kimlik doğrulama iş kurallarını tanımlar. */
public interface AuthService {

    /**
     * Kullanıcı girişi yapar.
     * 2FA kapalı: JWT token döner.
     * 2FA açık: requiresTwoFactor=true ve challengeToken döner; tam JWT verilmez.
     */
    AuthResponse login(LoginRequest request);

    /** TOTP secret üretir, kullanıcıya kaydeder; QR URI ve secret döner. */
    TotpSetupResponse setupTotp(String email);

    /** Kod doğruluysa 2FA'yı aktif eder; yanlış kodda BusinessRuleException fırlatır. */
    void enableTotp(String email, EnableTotpRequest request);

    /**
     * Challenge token'ı + TOTP kodunu doğrular; her ikisi geçerliyse tam JWT döner.
     * Geçersiz durumda BusinessRuleException fırlatır.
     */
    AuthResponse verifyTotp(VerifyTotpRequest request);
}

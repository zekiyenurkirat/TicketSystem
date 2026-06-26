package com.ticketsystem.service;

/** RFC 6238 TOTP üretme ve doğrulama işlemlerini tanımlar. */
public interface TotpService {

    /** 20 byte rastgele veri üretip Base32 kodlar; Google Authenticator uyumlu secret döner. */
    String generateSecret();

    /**
     * Verilen TOTP kodunu secret'a göre doğrular.
     * ±1 zaman penceresi toleransı uygulanır (30 saniyelik period).
     */
    boolean validateCode(String secret, String code);

    /** Google Authenticator / Authy'nin okuyabileceği otpauth:// URI üretir. */
    String buildOtpAuthUri(String secret, String email, String issuer);
}

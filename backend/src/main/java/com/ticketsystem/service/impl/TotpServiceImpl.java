package com.ticketsystem.service.impl;

import com.ticketsystem.service.TotpService;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * RFC 6238 TOTP implementasyonu — dış kütüphane kullanılmaz.
 * Base32 encode/decode ve HMAC-SHA1 hesaplama tamamen JDK ile yapılır.
 */
@Service
public class TotpServiceImpl implements TotpService {

    private static final int    DIGITS = 6;
    private static final int    PERIOD = 30;           // saniye
    private static final String ALGORITHM = "HmacSHA1";
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    @Override
    public String generateSecret() {
        byte[] bytes = new byte[20];                   // 160 bit — RFC önerisi
        new SecureRandom().nextBytes(bytes);
        return base32Encode(bytes);
    }

    @Override
    public boolean validateCode(String secret, String code) {
        if (code == null || code.length() != DIGITS) return false;
        int inputOtp;
        try {
            inputOtp = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            return false;
        }
        try {
            byte[] keyBytes = base32Decode(secret);
            long timeStep = Instant.now().getEpochSecond() / PERIOD;
            // T-1, T, T+1 — saat kayması toleransı
            for (long t = timeStep - 1; t <= timeStep + 1; t++) {
                if (generateOtp(keyBytes, t) == inputOtp) return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public String buildOtpAuthUri(String secret, String email, String issuer) {
        return "otpauth://totp/" + issuer + ":" + email
                + "?secret=" + secret
                + "&issuer=" + issuer
                + "&algorithm=SHA1&digits=" + DIGITS + "&period=" + PERIOD;
    }

    // ── RFC 6238 core ────────────────────────────────────────────────────────

    private int generateOtp(byte[] key, long timeStep) throws Exception {
        byte[] msg = ByteBuffer.allocate(8).putLong(timeStep).array();
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(key, ALGORITHM));
        byte[] hash = mac.doFinal(msg);
        // Dynamic truncation (RFC 4226 §5.4)
        int offset = hash[hash.length - 1] & 0xF;
        int binary = ((hash[offset]     & 0x7F) << 24)
                   | ((hash[offset + 1] & 0xFF) << 16)
                   | ((hash[offset + 2] & 0xFF) << 8)
                   |  (hash[offset + 3] & 0xFF);
        return binary % (int) Math.pow(10, DIGITS);
    }

    // ── Base32 (RFC 4648) ────────────────────────────────────────────────────

    private String base32Encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0, bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                sb.append(BASE32_ALPHABET.charAt((buffer >> bitsLeft) & 0x1F));
            }
        }
        if (bitsLeft > 0) {
            sb.append(BASE32_ALPHABET.charAt((buffer << (5 - bitsLeft)) & 0x1F));
        }
        return sb.toString();
    }

    private byte[] base32Decode(String input) {
        String clean = input.toUpperCase().replace("=", "");
        byte[] result = new byte[clean.length() * 5 / 8];
        int buffer = 0, bitsLeft = 0, idx = 0;
        for (char c : clean.toCharArray()) {
            int val = BASE32_ALPHABET.indexOf(c);
            if (val < 0) throw new IllegalArgumentException("Geçersiz Base32 karakter: " + c);
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                result[idx++] = (byte) (buffer >> bitsLeft);
            }
        }
        return result;
    }
}

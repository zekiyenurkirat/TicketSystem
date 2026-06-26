package com.ticketsystem.dto.response;

import com.ticketsystem.entity.enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Kimlik doğrulama yanıtı.
 *
 * 2FA kapalı kullanıcı: token/email/role/expiresIn dolu, requiresTwoFactor=false.
 * 2FA açık kullanıcı:   token=null, role=null, expiresIn=null, requiresTwoFactor=true,
 *                       challengeToken kısa ömürlü JWT ile dolu.
 */
@Getter
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private String email;
    private Role   role;
    private Long   expiresIn;
    private boolean requiresTwoFactor;
    /** Kısa ömürlü (5 dk) JWT; yalnızca requiresTwoFactor=true durumunda set edilir. */
    private String challengeToken;

    /** 2FA kapalı normal login veya TOTP doğrulama sonrası tam yanıt. */
    public static AuthResponse fullAuth(String token, String email, Role role, Long expiresIn) {
        AuthResponse r = new AuthResponse();
        r.token = token;
        r.email = email;
        r.role  = role;
        r.expiresIn = expiresIn;
        r.requiresTwoFactor = false;
        return r;
    }

    /** 2FA açık kullanıcı için parola doğrulandı, TOTP kodu bekleniyor. */
    public static AuthResponse challenge(String email, String challengeToken) {
        AuthResponse r = new AuthResponse();
        r.email = email;
        r.requiresTwoFactor = true;
        r.challengeToken    = challengeToken;
        return r;
    }
}

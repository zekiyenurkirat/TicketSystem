package com.ticketsystem.dto.response;

import com.ticketsystem.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Başarılı kimlik doğrulama sonucunu taşıyan yanıt nesnesi. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String email;
    private Role role;
    private Long expiresIn;
}

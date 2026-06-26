package com.ticketsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** TOTP doğrulama isteği: login'den dönen challenge token + 6 haneli kod. */
@Getter
@NoArgsConstructor
public class VerifyTotpRequest {

    /** Login yanıtındaki kısa ömürlü (5 dk) JWT challenge token. */
    @NotBlank
    private String challengeToken;

    @NotBlank
    @Pattern(regexp = "\\d{6}", message = "TOTP kodu 6 haneli sayı olmalıdır.")
    private String code;
}

package com.ticketsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 2FA aktivasyon isteği: Google Authenticator'dan alınan 6 haneli kod. */
@Getter
@NoArgsConstructor
public class EnableTotpRequest {

    @NotBlank
    @Pattern(regexp = "\\d{6}", message = "TOTP kodu 6 haneli sayı olmalıdır.")
    private String code;
}

package com.ticketsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 2FA kurulum yanıtı: secret (Base32) ve Google Authenticator URI. */
@Getter
@AllArgsConstructor
public class TotpSetupResponse {
    private String secret;
    private String otpAuthUri;
}

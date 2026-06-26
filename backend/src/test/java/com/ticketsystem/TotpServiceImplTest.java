package com.ticketsystem;

import com.ticketsystem.service.impl.TotpServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TotpServiceImplTest {

    private TotpServiceImpl totpService;

    @BeforeEach
    void setUp() {
        totpService = new TotpServiceImpl();
    }

    @Test
    void generateSecret_returnsNonNullBase32String() {
        String secret = totpService.generateSecret();
        assertThat(secret).isNotBlank();
        // Base32 alfabesi: A-Z ve 2-7
        assertThat(secret).matches("[A-Z2-7]+");
    }

    @Test
    void generateSecret_returnsDifferentValuesEachCall() {
        String s1 = totpService.generateSecret();
        String s2 = totpService.generateSecret();
        assertThat(s1).isNotEqualTo(s2);
    }

    @Test
    void validateCode_returnsFalseForNull() {
        String secret = totpService.generateSecret();
        assertThat(totpService.validateCode(secret, null)).isFalse();
    }

    @Test
    void validateCode_returnsFalseForWrongLength() {
        String secret = totpService.generateSecret();
        assertThat(totpService.validateCode(secret, "12345")).isFalse();
        assertThat(totpService.validateCode(secret, "1234567")).isFalse();
    }

    @Test
    void validateCode_returnsFalseForNonNumeric() {
        String secret = totpService.generateSecret();
        assertThat(totpService.validateCode(secret, "abcdef")).isFalse();
    }

    @Test
    void validateCode_returnsFalseForObviouslyWrongCode() {
        String secret = totpService.generateSecret();
        // 000000 olasılığı teorik olarak 1/1000000; yanlış secret ile neredeyse kesinlikle false
        assertThat(totpService.validateCode(secret, "000000")).isFalse();
    }

    @Test
    void buildOtpAuthUri_containsExpectedComponents() {
        String secret = "TESTSECRETBASE32AA";
        String email  = "user@example.com";
        String issuer = "TicketSystem";
        String uri    = totpService.buildOtpAuthUri(secret, email, issuer);

        assertThat(uri).startsWith("otpauth://totp/");
        assertThat(uri).contains("secret=" + secret);
        assertThat(uri).contains("issuer=" + issuer);
        assertThat(uri).contains(email);
        assertThat(uri).contains("algorithm=SHA1");
        assertThat(uri).contains("digits=6");
        assertThat(uri).contains("period=30");
    }
}

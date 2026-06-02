package com.ticketsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Yeni kayıt talebi oluşturma isteği için veri taşıyıcı. Yalnızca CUSTOMER kaydı için kullanılır. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRegistrationRequestRequest {

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;
}

package com.ticketsystem.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Kayıt talebi red isteği için veri taşıyıcı. */
@Getter
@Setter
@NoArgsConstructor
public class RejectRegistrationRequestRequest {

    private String note;
}

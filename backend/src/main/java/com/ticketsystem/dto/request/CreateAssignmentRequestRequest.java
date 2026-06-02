package com.ticketsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Atama isteği oluşturma için veri taşıyıcı. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentRequestRequest {

    @NotNull
    private Long ticketId;

    private String note;
}

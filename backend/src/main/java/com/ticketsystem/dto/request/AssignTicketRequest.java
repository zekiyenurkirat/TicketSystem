package com.ticketsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Ticket atama isteği için veri taşıyıcı. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignTicketRequest {

    @NotNull
    private Long agentId;
}

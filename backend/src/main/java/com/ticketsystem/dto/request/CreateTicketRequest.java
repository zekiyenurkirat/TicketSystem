package com.ticketsystem.dto.request;

import com.ticketsystem.entity.enums.Impact;
import com.ticketsystem.entity.enums.Priority;
import com.ticketsystem.entity.enums.Urgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Yeni ticket oluşturma isteği için veri taşıyıcı. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {

    @NotNull
    private Long createdById;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Priority customerPriority;

    @NotNull
    private Impact impact;

    @NotNull
    private Urgency urgency;
}

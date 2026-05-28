package com.ticketsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dosya eki kaydetme isteği için veri taşıyıcı. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaveAttachmentRequest {

    @NotNull
    private Long ticketId;

    @NotNull
    private Long uploadedById;

    @NotBlank
    private String fileName;

    @NotBlank
    private String fileType;

    @NotBlank
    private String filePath;

    @NotNull
    @Positive
    private Long fileSize;
}

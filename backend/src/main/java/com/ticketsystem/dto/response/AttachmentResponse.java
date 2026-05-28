package com.ticketsystem.dto.response;

import com.ticketsystem.entity.Attachment;
import com.ticketsystem.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Dosya eki bilgilerini dışarıya taşıyan yanıt nesnesi. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {

    private Long id;
    private Long ticketId;
    private Long uploadedById;
    private String uploaderFullName;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime createdAt;

    /** {@link Attachment} entity'sinden {@link AttachmentResponse} üretir. */
    public static AttachmentResponse from(Attachment attachment) {
        Long ticketId = attachment.getTicket() != null ? attachment.getTicket().getId() : null;

        User uploadedBy = attachment.getUploadedBy();
        Long uploadedById = uploadedBy != null ? uploadedBy.getId() : null;
        String uploaderFullName = uploadedBy != null
                ? uploadedBy.getFirstName() + " " + uploadedBy.getLastName()
                : null;

        return new AttachmentResponse(
                attachment.getId(),
                ticketId,
                uploadedById,
                uploaderFullName,
                attachment.getFileName(),
                attachment.getFileType(),
                attachment.getFileSize(),
                attachment.getCreatedAt()
        );
    }
}

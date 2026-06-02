package com.ticketsystem.dto.response;

import com.ticketsystem.entity.AssignmentRequest;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.AssignmentRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Atama isteği bilgilerini dışarıya taşıyan yanıt nesnesi. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequestResponse {

    private Long id;
    private Long ticketId;
    private String ticketNumber;
    private Long requestedById;
    private String requestedByFullName;
    private AssignmentRequestStatus status;
    private String note;
    private Long reviewedById;
    private String reviewedByFullName;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** {@link AssignmentRequest} entity'sinden {@link AssignmentRequestResponse} üretir. */
    public static AssignmentRequestResponse from(AssignmentRequest req) {
        User reviewedBy = req.getReviewedBy();
        User requestedBy = req.getRequestedBy();

        return new AssignmentRequestResponse(
                req.getId(),
                req.getTicket().getId(),
                req.getTicket().getTicketNumber(),
                requestedBy.getId(),
                requestedBy.getFirstName() + " " + requestedBy.getLastName(),
                req.getStatus(),
                req.getNote(),
                reviewedBy != null ? reviewedBy.getId() : null,
                reviewedBy != null ? reviewedBy.getFirstName() + " " + reviewedBy.getLastName() : null,
                req.getReviewedAt(),
                req.getCreatedAt(),
                req.getUpdatedAt()
        );
    }
}

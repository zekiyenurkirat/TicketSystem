package com.ticketsystem.dto.response;

import com.ticketsystem.entity.RegistrationRequest;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.RegistrationRequestStatus;
import com.ticketsystem.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Kayıt talebi bilgilerini dışarıya taşıyan yanıt nesnesi. passwordHash dahil edilmez. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequestResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role requestedRole;
    private RegistrationRequestStatus status;
    private String note;
    private Long reviewedById;
    private String reviewedByFullName;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** {@link RegistrationRequest} entity'sinden {@link RegistrationRequestResponse} üretir. */
    public static RegistrationRequestResponse from(RegistrationRequest req) {
        User reviewedBy = req.getReviewedBy();
        return new RegistrationRequestResponse(
                req.getId(),
                req.getFirstName(),
                req.getLastName(),
                req.getEmail(),
                req.getRequestedRole(),
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

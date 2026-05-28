package com.ticketsystem.dto.response;

import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.Priority;
import com.ticketsystem.entity.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Ticket bilgilerini dışarıya taşıyan yanıt nesnesi. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private TicketStatus status;
    private Priority priority;
    private Long createdById;
    private String createdByFullName;
    private Long assignedToId;
    private String assignedToFullName;
    private LocalDateTime dueDate;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** {@link Ticket} entity'sinden {@link TicketResponse} üretir. */
    public static TicketResponse from(Ticket ticket) {
        User createdBy = ticket.getCreatedBy();
        Long createdById = createdBy != null ? createdBy.getId() : null;
        String createdByFullName = createdBy != null
                ? createdBy.getFirstName() + " " + createdBy.getLastName()
                : null;

        User assignedTo = ticket.getAssignedTo();
        Long assignedToId = assignedTo != null ? assignedTo.getId() : null;
        String assignedToFullName = assignedTo != null
                ? assignedTo.getFirstName() + " " + assignedTo.getLastName()
                : null;

        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                createdById,
                createdByFullName,
                assignedToId,
                assignedToFullName,
                ticket.getDueDate(),
                ticket.getResolvedAt(),
                ticket.getClosedAt(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}

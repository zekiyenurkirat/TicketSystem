package com.ticketsystem.entity;

import com.ticketsystem.entity.enums.AssignmentRequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Agent'ın atanmamış bir ticket için oluşturduğu atama isteğini temsil eder.
 * "assignment_requests" tablosuna karşılık gelir.
 */
@Entity
@Table(name = "assignment_requests")
@Getter
@Setter
@NoArgsConstructor
public class AssignmentRequest extends BaseEntity {

    /** İsteğin ilişkilendirildiği ticket. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /** İsteği oluşturan agent. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    /** İsteğin mevcut durumu. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssignmentRequestStatus status = AssignmentRequestStatus.PENDING;

    /** Agent'ın eklediği isteğe dair açıklama notu. */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /** İsteği onaylayan veya reddeden manager. İşlem yapılmamışsa null. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id", nullable = true)
    private User reviewedBy;

    /** Onay veya reddin gerçekleştiği zaman. İşlem yapılmamışsa null. */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}

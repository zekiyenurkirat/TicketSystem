package com.ticketsystem.entity;

import com.ticketsystem.entity.enums.Priority;
import com.ticketsystem.entity.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Bir destek talebini temsil eden ana entity.
 * "tickets" tablosuna karşılık gelir.
 */
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
public class Ticket extends BaseEntity {

    /** İnsan tarafından okunabilir ticket numarası. Örn: TK-0001 */
    @Column(name = "ticket_number", nullable = false, unique = true)
    private String ticketNumber;

    @Column(name = "title", nullable = false)
    private String title;

    /** Sorunun detaylı açıklaması. */
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /** Ticket'ın mevcut durumu. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status;

    /** Ticket'ın öncelik seviyesi. */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    /** Ticket'ı oluşturan kullanıcı. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    /** Ticket'ı çözmekle görevlendirilen agent. Yeni ticket'larda null olabilir. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id", nullable = true)
    private User assignedTo;

    /** Ticket'a yapılan yorumlar. */
    @OneToMany(mappedBy = "ticket", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Comment> comments = new ArrayList<>();

    /** Ticket'a eklenen dosyalar. */
    @OneToMany(mappedBy = "ticket", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Attachment> attachments = new ArrayList<>();

    /** SLA son tarihi. Ticket oluşturulduğunda priority'e göre hesaplanır. */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    /** Ticket'ın RESOLVED statüsüne geçtiği zaman. */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /** Ticket'ın CLOSED statüsüne geçtiği zaman. */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}

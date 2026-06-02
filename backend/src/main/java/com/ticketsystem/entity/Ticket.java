package com.ticketsystem.entity;

import com.ticketsystem.entity.enums.Impact;
import com.ticketsystem.entity.enums.Priority;
import com.ticketsystem.entity.enums.TicketStatus;
import com.ticketsystem.entity.enums.Urgency;
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

    /** Ticket'ın aktif/final öncelik seviyesi. SLA hesaplamasının dayanağıdır. */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    /** Ticket oluşturulurken creator tarafından seçilen öncelik seviyesi. */
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_priority")
    private Priority customerPriority;

    /** Ticket oluşturulurken bildirilen iş etkisi seviyesi. */
    @Enumerated(EnumType.STRING)
    @Column(name = "impact")
    private Impact impact;

    /** Ticket oluşturulurken bildirilen aciliyet seviyesi. */
    @Enumerated(EnumType.STRING)
    @Column(name = "urgency")
    private Urgency urgency;

    /** Impact ve urgency matrisinden sistem tarafından hesaplanan önerilen öncelik. */
    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_priority")
    private Priority suggestedPriority;

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

    /** AGENT veya MANAGER tarafından priority review sırasında eklenen triage notu. */
    @Column(name = "priority_review_note", columnDefinition = "TEXT")
    private String priorityReviewNote;

    /** Priority review işleminin gerçekleştiği zaman. */
    @Column(name = "priority_reviewed_at")
    private LocalDateTime priorityReviewedAt;

    /** Priority review işlemini gerçekleştiren kullanıcı (AGENT veya MANAGER). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "priority_reviewed_by_id", nullable = true)
    private User priorityReviewedBy;

    /** jBPM process instance kimliği. Ticket oluşturulduğunda doldurulur.
     *  In-memory KieSession restart sonrası sıfırlanır; bu bilinen bir sınırlamadır. */
    @Column(name = "process_instance_id")
    private String processInstanceId;
}

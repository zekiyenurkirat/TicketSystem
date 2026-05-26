package com.ticketsystem.entity;

import com.ticketsystem.entity.enums.Priority;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Belirli bir öncelik seviyesi için SLA kurallarını tanımlayan entity.
 * "sla_rules" tablosuna karşılık gelir.
 * Her Priority için yalnızca bir kayıt bulunabilir.
 */
@Entity
@Table(name = "sla_rules")
@Getter
@Setter
@NoArgsConstructor
public class SlaRule extends BaseEntity {

    /** Bu SLA kuralının geçerli olduğu öncelik seviyesi. */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, unique = true)
    private Priority priority;

    /** İlk müdahale için izin verilen maksimum süre (saat). */
    @Column(name = "response_time_hours", nullable = false)
    private Integer responseTimeHours;

    /** Ticket'ın çözüme kavuşması için izin verilen maksimum süre (saat). */
    @Column(name = "resolution_time_hours", nullable = false)
    private Integer resolutionTimeHours;

    /** Bu süre aşıldığında MANAGER'a eskalasyon bildirimi gönderilir (saat). */
    @Column(name = "escalation_time_hours", nullable = false)
    private Integer escalationTimeHours;

    /**
     * Kuralın aktif olup olmadığını belirtir.
     * false → Kural devre dışı; SLA hesaplamalarına dahil edilmez.
     */
    @Column(name = "active", nullable = false)
    private boolean active = true;
}

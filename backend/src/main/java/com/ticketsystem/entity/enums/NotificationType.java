package com.ticketsystem.entity.enums;

/**
 * Bildirim tipi.
 * Faz 13a-2 (EscalationService) ve Faz 13b (AssignmentRequest) için
 * yeni değerler buraya eklenir.
 */
public enum NotificationType {

    /** SLA çözüm süresi doldu; ticket hâlâ aktif statüde. */
    SLA_BREACHED,

    /** SLA çözüm süresine escalation eşiği kadar veya daha az kaldı. */
    SLA_APPROACHING,

    /** CRITICAL/BLOCKER öncelikli ticket NEW statüsünde atanmamış bekliyor. */
    UNASSIGNED_CRITICAL,

    /** Bir ticket agent'a atandı. */
    TICKET_ASSIGNED,

    /** Agent, atanmamış bir ticket için atama isteği oluşturdu; tüm aktif Manager'lara gönderilir. */
    ASSIGNMENT_REQUEST_CREATED,

    /** Manager, agent'ın atama isteğini onayladı; ilgili agent'a gönderilir. */
    ASSIGNMENT_REQUEST_APPROVED,

    /** Manager, agent'ın atama isteğini reddetti; ilgili agent'a gönderilir. */
    ASSIGNMENT_REQUEST_REJECTED
}

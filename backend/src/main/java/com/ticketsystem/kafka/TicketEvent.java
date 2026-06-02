package com.ticketsystem.kafka;

import com.ticketsystem.entity.enums.TicketStatus;

/**
 * Kafka topic'e gönderilen ticket yaşam döngüsü eventi.
 * timestamp ISO-8601 string olarak tutulur — Jackson config bağımsızlığı sağlar.
 */
public record TicketEvent(
        Long ticketId,
        String ticketNumber,
        String action,          // "CREATE" | "ASSIGN" | "STATUS_CHANGE"
        TicketStatus oldStatus, // CREATE eventinde null
        TicketStatus newStatus,
        Long userId,            // İşlemi yapan kullanıcının DB ID'si
        String timestamp        // Instant.now().toString() — örn: "2026-06-03T10:30:00.123Z"
) {}

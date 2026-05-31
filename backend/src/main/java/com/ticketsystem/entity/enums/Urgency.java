package com.ticketsystem.entity.enums;

/**
 * Ticket'ın ne kadar hızlı ele alınması gerektiğini tanımlar.
 * suggestedPriority hesaplamasında Impact ile birlikte kullanılır.
 *
 * LOW    : Ertelenebilir; iş akışı devam edebiliyor.
 * MEDIUM : Kısa sürede çözülmeli; geçici çözüm mevcut.
 * HIGH   : Acil müdahale gerekiyor; iş akışı durma noktasında.
 */
public enum Urgency {
    LOW,
    MEDIUM,
    HIGH
}

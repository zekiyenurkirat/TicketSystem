package com.ticketsystem.entity.enums;

/**
 * Ticket'ın iş süreçlerine etkisini tanımlar.
 * suggestedPriority hesaplamasında Urgency ile birlikte kullanılır.
 *
 * LOW    : Sınırlı etki; yalnızca tek kullanıcı veya küçük bir grup etkileniyor.
 * MEDIUM : Orta etki; birden fazla kullanıcı veya bir departman etkileniyor.
 * HIGH   : Geniş etki; tüm kuruluş veya kritik iş akışı etkileniyor.
 */
public enum Impact {
    LOW,
    MEDIUM,
    HIGH
}

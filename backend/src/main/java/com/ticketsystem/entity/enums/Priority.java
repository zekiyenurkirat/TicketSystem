package com.ticketsystem.entity.enums;

/**
 * Ticket öncelik seviyelerini tanımlar.
 * SLA süreleri (müdahale, çözüm, eskalasyon) bu önceliğe göre belirlenir.
 *
 * BLOCKER  : Sistemi tamamen durduran, iş akışını engelleyen kritik sorun.
 * CRITICAL : İş akışını ciddi ölçüde etkileyen, acil müdahale gerektiren sorun.
 * HIGH     : Önemli etkisi olan, geçici çözümü mümkün sorun.
 * MEDIUM   : Orta düzeyli etki, normal iş akışı içinde ele alınabilir.
 * LOW      : Düşük öncelikli, ertelenebilir veya geliştirme isteği.
 */
public enum Priority {
    BLOCKER,
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

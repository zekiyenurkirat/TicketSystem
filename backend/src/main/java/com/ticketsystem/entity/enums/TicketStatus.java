package com.ticketsystem.entity.enums;

/**
 * Bir ticket'ın yaşam döngüsündeki olası durumlarını tanımlar.
 *
 * NEW                  : Yeni oluşturulmuş, henüz atanmamış.
 * ASSIGNED             : Bir agent'a atanmış, çalışma başlamadı.
 * IN_PROGRESS          : Agent üzerinde aktif olarak çalışıyor.
 * WAITING_FOR_CUSTOMER : Agent müşteriden ek bilgi veya onay bekliyor.
 * RESOLVED             : Çözüm uygulandı, müşteri onayı bekleniyor.
 * CLOSED               : Müşteri onayladı veya otomatik kapatıldı.
 * CANCELLED            : Talep iptal edildi.
 */
public enum TicketStatus {
    NEW,
    ASSIGNED,
    IN_PROGRESS,
    WAITING_FOR_CUSTOMER,
    RESOLVED,
    CLOSED,
    CANCELLED
}

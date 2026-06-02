package com.ticketsystem.service;

import com.ticketsystem.dto.response.NotificationResponse;
import com.ticketsystem.entity.enums.NotificationType;

import java.util.List;

/** Bildirim yönetimi iş mantığı arayüzü. */
public interface NotificationService {

    /**
     * Oturum açmış kullanıcının son 50 bildirimini yeniden eskiye sıralı döner.
     */
    List<NotificationResponse> getMyNotifications();

    /**
     * Belirtilen bildirimi okundu olarak işaretler.
     * Bildirim bulunamazsa {@code ResourceNotFoundException} (404),
     * başkasının bildirimine erişilirse {@code AccessDeniedException} (403) fırlatır.
     */
    void markAsSeen(Long notificationId);

    /**
     * Oturum açmış kullanıcının tüm okunmamış bildirimlerini okundu olarak işaretler.
     */
    void markAllAsSeen();

    /**
     * Yeni bildirim oluşturur ve kaydeder.
     * Faz 13a-2 EscalationService tarafından çağrılacaktır.
     *
     * @param userId      bildirimin sahibi kullanıcı ID'si
     * @param type        bildirim tipi
     * @param message     kullanıcıya gösterilecek mesaj metni
     * @param referenceId ilgili ticket ID'si; bağlı ticket yoksa null
     */
    void createNotification(Long userId, NotificationType type, String message, Long referenceId);
}

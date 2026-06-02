package com.ticketsystem.repository;

import com.ticketsystem.entity.Notification;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** {@link Notification} entity'si için JPA repository. */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Kullanıcının son 50 bildirimini yeniden eskiye sıralar. */
    List<Notification> findTop50ByUserOrderByCreatedAtDesc(User user);

    /** Kullanıcının tüm okunmamış bildirimlerini yeniden eskiye sıralar. */
    List<Notification> findByUserAndSeenFalseOrderByCreatedAtDesc(User user);

    /**
     * Aynı (kullanıcı, tip, referenceId) kombinasyonunda okunmamış bildirim
     * var mı kontrol eder.
     * Faz 13a-2 EscalationService tarafından idempotency için kullanılır.
     */
    boolean existsByUserAndTypeAndReferenceIdAndSeenFalse(
            User user, NotificationType type, Long referenceId);
}

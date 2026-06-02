package com.ticketsystem.entity;

import com.ticketsystem.entity.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Kullanıcıya iletilen bir bildirimi temsil eder.
 * "notifications" tablosuna karşılık gelir.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification extends BaseEntity {

    /** Bildirimin sahibi kullanıcı. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Bildirim tipi. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    /** Kullanıcıya gösterilecek hazır mesaj metni. */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * İlgili ticket'ın ID'si.
     * Nullable — bildirim belirli bir ticket'a bağlı olmayabilir.
     * FK kısıtı eklenmedi; ticket silinse bile bildirim korunur.
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /** Kullanıcının bildirimi görüp görmediği. Varsayılan: false. */
    @Column(name = "seen", nullable = false)
    private boolean seen = false;
}

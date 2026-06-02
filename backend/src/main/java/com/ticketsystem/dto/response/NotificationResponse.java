package com.ticketsystem.dto.response;

import com.ticketsystem.entity.Notification;
import com.ticketsystem.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Bildirim bilgilerini dışarıya taşıyan yanıt nesnesi. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String message;
    private Long referenceId;
    private boolean seen;
    private LocalDateTime createdAt;

    /** {@link Notification} entity'sinden {@link NotificationResponse} üretir. */
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.isSeen(),
                notification.getCreatedAt()
        );
    }
}

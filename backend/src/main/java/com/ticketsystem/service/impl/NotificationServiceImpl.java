package com.ticketsystem.service.impl;

import com.ticketsystem.core.exception.ResourceNotFoundException;
import com.ticketsystem.dto.response.NotificationResponse;
import com.ticketsystem.entity.Notification;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.NotificationType;
import com.ticketsystem.repository.NotificationRepository;
import com.ticketsystem.service.NotificationService;
import com.ticketsystem.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** {@link NotificationService} arayüzünün varsayılan uygulaması. */
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() {
        User currentUser = getCurrentUser();
        return notificationRepository.findTop50ByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void markAsSeen(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bildirim bulunamadı. id: " + notificationId));

        User currentUser = getCurrentUser();
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bu bildirimi işaretleme yetkiniz yok.");
        }

        notification.setSeen(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsSeen() {
        User currentUser = getCurrentUser();
        List<Notification> unseen =
                notificationRepository.findByUserAndSeenFalseOrderByCreatedAtDesc(currentUser);
        unseen.forEach(n -> n.setSeen(true));
        notificationRepository.saveAll(unseen);
    }

    @Override
    @Transactional
    public void createNotification(Long userId, NotificationType type,
                                   String message, Long referenceId) {
        User user = userService.getUserById(userId);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setReferenceId(referenceId);
        notification.setSeen(false);

        notificationRepository.save(notification);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }
}

package com.ticketsystem.service;

import com.ticketsystem.entity.SlaRule;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.NotificationType;
import com.ticketsystem.entity.enums.Priority;
import com.ticketsystem.entity.enums.Role;
import com.ticketsystem.entity.enums.TicketStatus;
import com.ticketsystem.repository.NotificationRepository;
import com.ticketsystem.repository.SlaRuleRepository;
import com.ticketsystem.repository.TicketRepository;
import com.ticketsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Her 5 dakikada bir çalışan escalation job'ı.
 * Açık ticket'ları SLA ve atama durumuna göre değerlendirir;
 * ilgili kullanıcılara bildirim üretir.
 */
@Slf4j
@Service
public class EscalationService {

    /** Ticket yaşam döngüsünün sonlandığı terminal statüler. Bu statüler için hiçbir alarm üretilmez. */
    private static final Set<TicketStatus> TERMINAL_STATUSES =
            EnumSet.of(TicketStatus.CLOSED, TicketStatus.CANCELLED);

    /** SLA kontrolünün anlamlı olduğu aktif statüler. RESOLVED dahil değil. */
    private static final Set<TicketStatus> SLA_ACTIVE_STATUSES =
            EnumSet.of(TicketStatus.NEW, TicketStatus.ASSIGNED,
                       TicketStatus.IN_PROGRESS, TicketStatus.WAITING_FOR_CUSTOMER);

    /** Atanmamış kritik kontrolü için izlenen öncelikler. */
    private static final Set<Priority> CRITICAL_PRIORITIES =
            EnumSet.of(Priority.BLOCKER, Priority.CRITICAL);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TicketRepository ticketRepository;
    private final SlaRuleRepository slaRuleRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public EscalationService(TicketRepository ticketRepository,
                              SlaRuleRepository slaRuleRepository,
                              UserRepository userRepository,
                              NotificationRepository notificationRepository,
                              NotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.slaRuleRepository = slaRuleRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    /**
     * Her 5 dakikada bir çalışır.
     * Terminal olmayan tüm ticket'ları değerlendirir; SLA ihlali,
     * SLA yaklaşması ve atanmamış kritik durumları için bildirim üretir.
     */
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void runEscalationCheck() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Escalation kontrolü başladı: {}", now.format(DATE_FORMATTER));

        List<Ticket> activeTickets = ticketRepository.findByStatusNotIn(TERMINAL_STATUSES);
        Map<Priority, SlaRule> slaRuleMap = loadActiveSlaRules();
        List<User> managers = userRepository.findByRoleAndActiveTrue(Role.MANAGER);

        int slaBreachedCount = 0;
        int slaApproachingCount = 0;
        int unassignedCriticalCount = 0;

        for (Ticket ticket : activeTickets) {

            // --- SLA kontrolleri ---
            if (ticket.getDueDate() != null && SLA_ACTIVE_STATUSES.contains(ticket.getStatus())) {
                LocalDateTime dueDate = ticket.getDueDate();
                SlaRule rule = slaRuleMap.get(ticket.getPriority());

                if (dueDate.isBefore(now)) {
                    // SLA süresi dolmuş
                    String message = buildBreachedMessage(ticket);
                    notifyAssignedAndManagers(ticket, NotificationType.SLA_BREACHED, message, managers);
                    slaBreachedCount++;

                } else if (rule != null) {
                    // SLA süresi henüz geçmemiş; escalation eşiğine girildi mi?
                    LocalDateTime threshold = dueDate.minusHours(rule.getEscalationTimeHours());
                    if (!now.isBefore(threshold)) {
                        String message = buildApproachingMessage(ticket);
                        notifyAssignedAndManagers(ticket, NotificationType.SLA_APPROACHING, message, managers);
                        slaApproachingCount++;
                    }
                }
            }

            // --- Atanmamış kritik kontrolü (SLA kontrolünden bağımsız) ---
            if (ticket.getAssignedTo() == null
                    && ticket.getStatus() == TicketStatus.NEW
                    && CRITICAL_PRIORITIES.contains(ticket.getPriority())) {
                String message = buildUnassignedCriticalMessage(ticket);
                notifyManagers(ticket, NotificationType.UNASSIGNED_CRITICAL, message, managers);
                unassignedCriticalCount++;
            }
        }

        log.info("Escalation kontrolü tamamlandı — SLA_BREACHED: {}, SLA_APPROACHING: {}, UNASSIGNED_CRITICAL: {}",
                slaBreachedCount, slaApproachingCount, unassignedCriticalCount);
    }

    // -------------------------------------------------------------------------
    // Yardımcı metodlar
    // -------------------------------------------------------------------------

    /**
     * SLA_BREACHED ve SLA_APPROACHING için atanan agent'ı (varsa) ve
     * tüm aktif MANAGER'ları bilgilendirir.
     */
    private void notifyAssignedAndManagers(Ticket ticket, NotificationType type,
                                            String message, List<User> managers) {
        if (ticket.getAssignedTo() != null) {
            notifyIfNotDuplicate(ticket.getAssignedTo(), type, message, ticket);
        }
        for (User manager : managers) {
            notifyIfNotDuplicate(manager, type, message, ticket);
        }
    }

    /** UNASSIGNED_CRITICAL için yalnızca tüm aktif MANAGER'ları bilgilendirir. */
    private void notifyManagers(Ticket ticket, NotificationType type,
                                 String message, List<User> managers) {
        for (User manager : managers) {
            notifyIfNotDuplicate(manager, type, message, ticket);
        }
    }

    /**
     * Kullanıcı için aynı tip ve ticket'a ait okunmamış bildirim yoksa yeni bildirim üretir.
     * Duplicate engeli: aynı (user, type, ticketId) kombinasyonunda unseen kayıt varsa atlanır.
     */
    private void notifyIfNotDuplicate(User user, NotificationType type,
                                       String message, Ticket ticket) {
        boolean exists = notificationRepository.existsByUserAndTypeAndReferenceIdAndSeenFalse(
                user, type, ticket.getId());
        if (!exists) {
            notificationService.createNotification(user.getId(), type, message, ticket.getId());
        }
    }

    /** Tüm aktif SLA kurallarını Priority → SlaRule haritasına yükler. */
    private Map<Priority, SlaRule> loadActiveSlaRules() {
        return slaRuleRepository.findAll()
                .stream()
                .filter(SlaRule::isActive)
                .collect(Collectors.toMap(SlaRule::getPriority, r -> r));
    }

    private String buildBreachedMessage(Ticket ticket) {
        return "[" + ticket.getTicketNumber() + "] SLA süresi doldu (bitiş: "
                + ticket.getDueDate().format(DATE_FORMATTER) + ").";
    }

    private String buildApproachingMessage(Ticket ticket) {
        return "[" + ticket.getTicketNumber() + "] SLA süresine az kaldı (bitiş: "
                + ticket.getDueDate().format(DATE_FORMATTER) + ").";
    }

    private String buildUnassignedCriticalMessage(Ticket ticket) {
        return "[" + ticket.getTicketNumber() + "] Kritik talep atanmamış bekliyor (öncelik: "
                + ticket.getPriority() + ").";
    }
}

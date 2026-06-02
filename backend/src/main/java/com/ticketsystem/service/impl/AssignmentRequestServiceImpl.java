package com.ticketsystem.service.impl;

import com.ticketsystem.core.exception.BusinessRuleException;
import com.ticketsystem.core.exception.ResourceNotFoundException;
import com.ticketsystem.dto.request.AssignTicketRequest;
import com.ticketsystem.dto.request.CreateAssignmentRequestRequest;
import com.ticketsystem.dto.response.AssignmentRequestResponse;
import com.ticketsystem.entity.AssignmentRequest;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.AssignmentRequestStatus;
import com.ticketsystem.entity.enums.NotificationType;
import com.ticketsystem.entity.enums.Role;
import com.ticketsystem.entity.enums.TicketStatus;
import com.ticketsystem.repository.AssignmentRequestRepository;
import com.ticketsystem.repository.UserRepository;
import com.ticketsystem.service.AssignmentRequestService;
import com.ticketsystem.service.NotificationService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** {@link AssignmentRequestService} arayüzünün varsayılan uygulaması. */
@Service
public class AssignmentRequestServiceImpl implements AssignmentRequestService {

    private final AssignmentRequestRepository assignmentRequestRepository;
    private final TicketService ticketService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AssignmentRequestServiceImpl(
            AssignmentRequestRepository assignmentRequestRepository,
            TicketService ticketService,
            UserService userService,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.assignmentRequestRepository = assignmentRequestRepository;
        this.ticketService = ticketService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public AssignmentRequestResponse createRequest(CreateAssignmentRequestRequest request) {
        User agent = getCurrentUser();
        Ticket ticket = ticketService.getTicketById(request.getTicketId());

        if (ticket.getStatus() != TicketStatus.NEW) {
            throw new BusinessRuleException(
                    "Yalnızca NEW statüsündeki ticket'lar için atama isteği oluşturabilirsiniz.");
        }
        if (ticket.getAssignedTo() != null) {
            throw new BusinessRuleException("Bu ticket zaten bir agent'a atanmış.");
        }
        if (assignmentRequestRepository.existsByRequestedByAndTicketAndStatus(
                agent, ticket, AssignmentRequestStatus.PENDING)) {
            throw new BusinessRuleException(
                    "Bu ticket için zaten bekleyen bir atama isteğiniz bulunuyor.");
        }

        AssignmentRequest assignmentRequest = new AssignmentRequest();
        assignmentRequest.setTicket(ticket);
        assignmentRequest.setRequestedBy(agent);
        assignmentRequest.setStatus(AssignmentRequestStatus.PENDING);
        assignmentRequest.setNote(request.getNote());

        AssignmentRequest saved = assignmentRequestRepository.save(assignmentRequest);

        String agentFullName = agent.getFirstName() + " " + agent.getLastName();
        String message = agentFullName + " '"
                + ticket.getTicketNumber() + "' numaralı ticket için atama isteği oluşturdu.";
        userRepository.findByRoleAndActiveTrue(Role.MANAGER)
                .forEach(manager -> notificationService.createNotification(
                        manager.getId(),
                        NotificationType.ASSIGNMENT_REQUEST_CREATED,
                        message,
                        ticket.getId()));

        return AssignmentRequestResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentRequestResponse> getRequests(AssignmentRequestStatus status) {
        return assignmentRequestRepository.findByStatus(status)
                .stream()
                .map(AssignmentRequestResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public AssignmentRequestResponse approveRequest(Long requestId) {
        AssignmentRequest assignmentRequest = assignmentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Atama isteği bulunamadı. id: " + requestId));

        if (assignmentRequest.getStatus() != AssignmentRequestStatus.PENDING) {
            throw new BusinessRuleException("Yalnızca PENDING statüsündeki istekler onaylanabilir.");
        }

        User manager = getCurrentUser();
        Ticket ticket = assignmentRequest.getTicket();
        User agent = assignmentRequest.getRequestedBy();

        // Diğer PENDING istekleri bul (bu onaylanmadan önce, DB'de henüz PENDING)
        List<AssignmentRequest> otherPending = assignmentRequestRepository
                .findByTicketAndStatus(ticket, AssignmentRequestStatus.PENDING)
                .stream()
                .filter(r -> !r.getId().equals(requestId))
                .toList();

        // Ticket'ı agent'a ata
        ticketService.assignTicket(ticket.getId(), new AssignTicketRequest(agent.getId()));

        // Bu isteği onayla
        assignmentRequest.setStatus(AssignmentRequestStatus.APPROVED);
        assignmentRequest.setReviewedBy(manager);
        assignmentRequest.setReviewedAt(LocalDateTime.now());
        assignmentRequestRepository.save(assignmentRequest);

        // Aynı ticket'ın diğer PENDING isteklerini otomatik reddet
        if (!otherPending.isEmpty()) {
            otherPending.forEach(other -> {
                other.setStatus(AssignmentRequestStatus.REJECTED);
                other.setReviewedBy(manager);
                other.setReviewedAt(LocalDateTime.now());
            });
            assignmentRequestRepository.saveAll(otherPending);
        }

        // Onaylanan agent'a bildirim gönder
        String message = "'" + ticket.getTicketNumber()
                + "' numaralı ticket için atama isteğiniz onaylandı.";
        notificationService.createNotification(
                agent.getId(),
                NotificationType.ASSIGNMENT_REQUEST_APPROVED,
                message,
                ticket.getId());

        return AssignmentRequestResponse.from(assignmentRequest);
    }

    @Override
    @Transactional
    public AssignmentRequestResponse rejectRequest(Long requestId) {
        AssignmentRequest assignmentRequest = assignmentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Atama isteği bulunamadı. id: " + requestId));

        if (assignmentRequest.getStatus() != AssignmentRequestStatus.PENDING) {
            throw new BusinessRuleException("Yalnızca PENDING statüsündeki istekler reddedilebilir.");
        }

        User manager = getCurrentUser();
        Ticket ticket = assignmentRequest.getTicket();
        User agent = assignmentRequest.getRequestedBy();

        assignmentRequest.setStatus(AssignmentRequestStatus.REJECTED);
        assignmentRequest.setReviewedBy(manager);
        assignmentRequest.setReviewedAt(LocalDateTime.now());
        AssignmentRequest saved = assignmentRequestRepository.save(assignmentRequest);

        // Reddedilen agent'a bildirim gönder
        String message = "'" + ticket.getTicketNumber()
                + "' numaralı ticket için atama isteğiniz reddedildi.";
        notificationService.createNotification(
                agent.getId(),
                NotificationType.ASSIGNMENT_REQUEST_REJECTED,
                message,
                ticket.getId());

        return AssignmentRequestResponse.from(saved);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }
}

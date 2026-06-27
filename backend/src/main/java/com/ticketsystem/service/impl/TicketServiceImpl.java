package com.ticketsystem.service.impl;

import com.ticketsystem.core.exception.BusinessRuleException;
import com.ticketsystem.core.exception.ResourceNotFoundException;
import com.ticketsystem.dto.request.AssignTicketRequest;
import com.ticketsystem.dto.request.ChangeStatusRequest;
import com.ticketsystem.dto.request.CreateTicketRequest;
import com.ticketsystem.dto.request.PriorityReviewRequest;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.Impact;
import com.ticketsystem.entity.enums.Priority;
import com.ticketsystem.entity.enums.Role;
import com.ticketsystem.entity.enums.Urgency;
import com.ticketsystem.entity.enums.TicketStatus;
import com.ticketsystem.repository.TicketRepository;
import com.ticketsystem.service.SlaService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.service.UserService;
import com.ticketsystem.kafka.TicketEvent;
import com.ticketsystem.kafka.TicketEventProducer;
import com.ticketsystem.workflow.TicketWorkflowService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** {@link TicketService} arayüzünün varsayılan uygulaması. */
@Service
public class TicketServiceImpl implements TicketService {

    private static final Map<TicketStatus, Set<TicketStatus>> VALID_TRANSITIONS;

    static {
        VALID_TRANSITIONS = new EnumMap<>(TicketStatus.class);
        VALID_TRANSITIONS.put(TicketStatus.NEW,
                EnumSet.of(TicketStatus.ASSIGNED, TicketStatus.CANCELLED));
        VALID_TRANSITIONS.put(TicketStatus.ASSIGNED,
                EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
        VALID_TRANSITIONS.put(TicketStatus.IN_PROGRESS,
                EnumSet.of(TicketStatus.WAITING_FOR_CUSTOMER, TicketStatus.RESOLVED, TicketStatus.CANCELLED));
        VALID_TRANSITIONS.put(TicketStatus.WAITING_FOR_CUSTOMER,
                EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
        VALID_TRANSITIONS.put(TicketStatus.RESOLVED,
                EnumSet.of(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS));
        VALID_TRANSITIONS.put(TicketStatus.CLOSED,
                EnumSet.noneOf(TicketStatus.class));
        VALID_TRANSITIONS.put(TicketStatus.CANCELLED,
                EnumSet.noneOf(TicketStatus.class));
    }

    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final SlaService slaService;
    private final TicketWorkflowService ticketWorkflowService;
    private final TicketEventProducer ticketEventProducer;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             UserService userService,
                             SlaService slaService,
                             TicketWorkflowService ticketWorkflowService,
                             TicketEventProducer ticketEventProducer) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.slaService = slaService;
        this.ticketWorkflowService = ticketWorkflowService;
        this.ticketEventProducer = ticketEventProducer;
    }

    @Override
    @Transactional
    public Ticket createTicket(CreateTicketRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.CUSTOMER
                && !currentUser.getId().equals(request.getCreatedById())) {
            throw new AccessDeniedException("Yalnızca kendi adınıza ticket oluşturabilirsiniz.");
        }
        if (currentUser.getRole() == Role.CUSTOMER
                && (request.getCustomerPriority() == Priority.CRITICAL
                        || request.getCustomerPriority() == Priority.BLOCKER)) {
            throw new BusinessRuleException("Müşteri yalnızca LOW, MEDIUM veya HIGH öncelik seçebilir.");
        }
        User createdBy = userService.getUserById(request.getCreatedById());
        Priority suggested = calculateSuggestedPriority(request.getImpact(), request.getUrgency());

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCustomerPriority(request.getCustomerPriority());
        ticket.setImpact(request.getImpact());
        ticket.setUrgency(request.getUrgency());
        ticket.setSuggestedPriority(suggested);
        ticket.setPriority(request.getCustomerPriority());
        ticket.setCreatedBy(createdBy);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setAssignedTo(null);
        ticket.setDueDate(slaService.calculateDueDate(ticket.getPriority()));

        Ticket saved = ticketRepository.save(ticket);
        // jBPM: Ticket için workflow process başlat (soft-fail)
        String pid = ticketWorkflowService.startProcess(saved.getId(), saved.getTicketNumber());
        if (pid != null) {
            saved.setProcessInstanceId(pid);
            saved = ticketRepository.save(saved);
        }
        // Kafka: ticket oluşturma eventi — DB save sonrası, soft-fail
        ticketEventProducer.send(new TicketEvent(
                saved.getId(), saved.getTicketNumber(),
                "CREATE", null, TicketStatus.NEW,
                currentUser.getId(), Instant.now().toString()));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Ticket getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket bulunamadı. id: " + id));
        validateTicketAccessForCustomer(ticket);
        return ticket;
    }

    @Override
    @Transactional(readOnly = true)
    public Ticket getTicketByTicketNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket bulunamadı. ticketNumber: " + ticketNumber));
        validateTicketAccessForCustomer(ticket);
        return ticket;
    }

    @Override
    @Transactional
    public Ticket assignTicket(Long ticketId, AssignTicketRequest request) {
        Ticket ticket = getTicketById(ticketId);
        User agent = userService.getUserById(request.getAgentId());

        if (agent.getRole() != Role.AGENT) {
            throw new BusinessRuleException("Atanacak kullanıcı AGENT rolünde olmalıdır. id: " + request.getAgentId());
        }
        if (!agent.isActive()) {
            throw new BusinessRuleException("Atanacak kullanıcı aktif değil. id: " + request.getAgentId());
        }

        TicketStatus currentStatus = ticket.getStatus();

        if (currentStatus == TicketStatus.NEW) {
            ticket.setAssignedTo(agent);
            ticket.setStatus(TicketStatus.ASSIGNED);
        } else if (currentStatus == TicketStatus.ASSIGNED) {
            ticket.setAssignedTo(agent);
        } else {
            throw new BusinessRuleException("Bu statüdeki ticket yeniden atanamaz: " + currentStatus);
        }

        Ticket result = ticketRepository.save(ticket);
        // jBPM: NEW→ASSIGNED geçişinde sinyal gönder (soft-fail)
        if (currentStatus == TicketStatus.NEW) {
            ticketWorkflowService.signalProcess(result.getProcessInstanceId(), TicketStatus.ASSIGNED);
        }
        // Kafka: atama eventi — DB save sonrası, soft-fail
        ticketEventProducer.send(new TicketEvent(
                result.getId(), result.getTicketNumber(),
                "ASSIGN", currentStatus, result.getStatus(),
                getCurrentUser().getId(), Instant.now().toString()));
        return result;
    }

    @Override
    @Transactional
    public Ticket changeStatus(Long ticketId, ChangeStatusRequest request) {
        Ticket ticket = getTicketById(ticketId);
        TicketStatus currentStatus = ticket.getStatus();
        TicketStatus newStatus = request.getNewStatus();

        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.CUSTOMER
                && !(currentStatus == TicketStatus.RESOLVED && newStatus == TicketStatus.CLOSED)) {
            throw new BusinessRuleException("Müşteri yalnızca çözümlenmiş kendi ticketını kapatabilir.");
        }
        if (currentUser.getRole() == Role.AGENT) {
            if (ticket.getAssignedTo() == null
                    || !ticket.getAssignedTo().getId().equals(currentUser.getId())) {
                throw new BusinessRuleException("Yalnızca size atanmış taleplerde statü değiştirebilirsiniz.");
            }
        }

        if (currentStatus == newStatus) {
            throw new BusinessRuleException("Ticket zaten bu statüde: " + currentStatus);
        }

        Set<TicketStatus> allowed = VALID_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new BusinessRuleException("Geçersiz statü geçişi: " + currentStatus + " -> " + newStatus);
        }

        if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        }

        ticket.setStatus(newStatus);
        Ticket result = ticketRepository.save(ticket);
        // jBPM: Yeni statü için sinyal gönder (soft-fail)
        ticketWorkflowService.signalProcess(result.getProcessInstanceId(), newStatus);
        // Kafka: statü değişikliği eventi — DB save sonrası, soft-fail
        ticketEventProducer.send(new TicketEvent(
                result.getId(), result.getTicketNumber(),
                "STATUS_CHANGE", currentStatus, newStatus,
                currentUser.getId(), Instant.now().toString()));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByPriority(Priority priority) {
        return ticketRepository.findByPriority(priority);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByCreatedBy(Long userId) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.CUSTOMER
                && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("Bu kullanıcının ticketlarını görüntüleme yetkiniz yok.");
        }
        User user = userService.getUserById(userId);
        return ticketRepository.findByCreatedBy(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByAssignedTo(Long agentId) {
        User agent = userService.getUserById(agentId);
        return ticketRepository.findByAssignedTo(agent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getUnassignedTickets() {
        return ticketRepository.findByAssignedToIsNull();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByStatusAndPriority(TicketStatus status, Priority priority) {
        return ticketRepository.findByStatusAndPriority(status, priority);
    }

    @Override
    @Transactional
    public Ticket reviewPriority(Long ticketId, PriorityReviewRequest request) {
        Ticket ticket = getTicketById(ticketId);
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.AGENT) {
            if (ticket.getAssignedTo() == null
                    || !ticket.getAssignedTo().getId().equals(currentUser.getId())) {
                throw new BusinessRuleException("Yalnızca size atanmış taleplerde öncelik incelemesi yapabilirsiniz.");
            }
        }
        ticket.setPriority(request.getPriority());
        ticket.setDueDate(slaService.calculateDueDate(request.getPriority()));
        ticket.setPriorityReviewNote(request.getReviewNote());
        ticket.setPriorityReviewedAt(LocalDateTime.now());
        ticket.setPriorityReviewedBy(currentUser);
        return ticketRepository.save(ticket);
    }

    private static Priority calculateSuggestedPriority(Impact impact, Urgency urgency) {
        return switch (impact) {
            case LOW    -> switch (urgency) {
                case LOW    -> Priority.LOW;
                case MEDIUM -> Priority.MEDIUM;
                case HIGH   -> Priority.HIGH;
            };
            case MEDIUM -> switch (urgency) {
                case LOW    -> Priority.MEDIUM;
                case MEDIUM -> Priority.HIGH;
                case HIGH   -> Priority.CRITICAL;
            };
            case HIGH   -> switch (urgency) {
                case LOW    -> Priority.HIGH;
                case MEDIUM -> Priority.CRITICAL;
                case HIGH   -> Priority.BLOCKER;
            };
        };
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }

    private void validateTicketAccessForCustomer(Ticket ticket) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.CUSTOMER
                && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bu ticket'a erişim yetkiniz yok.");
        }
    }

    private String generateTicketNumber() {
        return "TK-" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }
}

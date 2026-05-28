package com.ticketsystem.service.impl;

import com.ticketsystem.dto.request.AssignTicketRequest;
import com.ticketsystem.dto.request.ChangeStatusRequest;
import com.ticketsystem.dto.request.CreateTicketRequest;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.Priority;
import com.ticketsystem.entity.enums.Role;
import com.ticketsystem.entity.enums.TicketStatus;
import com.ticketsystem.repository.TicketRepository;
import com.ticketsystem.service.SlaService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public TicketServiceImpl(TicketRepository ticketRepository,
                             UserService userService,
                             SlaService slaService) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.slaService = slaService;
    }

    @Override
    @Transactional
    public Ticket createTicket(CreateTicketRequest request) {
        User createdBy = userService.getUserById(request.getCreatedById());

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setCreatedBy(createdBy);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setAssignedTo(null);
        ticket.setDueDate(slaService.calculateDueDate(request.getPriority()));

        return ticketRepository.save(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket bulunamadı. id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Ticket getTicketByTicketNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new RuntimeException("Ticket bulunamadı. ticketNumber: " + ticketNumber));
    }

    @Override
    @Transactional
    public Ticket assignTicket(Long ticketId, AssignTicketRequest request) {
        Ticket ticket = getTicketById(ticketId);
        User agent = userService.getUserById(request.getAgentId());

        if (agent.getRole() != Role.AGENT) {
            throw new RuntimeException("Atanacak kullanıcı AGENT rolünde olmalıdır. id: " + request.getAgentId());
        }
        if (!agent.isActive()) {
            throw new RuntimeException("Atanacak kullanıcı aktif değil. id: " + request.getAgentId());
        }

        TicketStatus currentStatus = ticket.getStatus();

        if (currentStatus == TicketStatus.NEW) {
            ticket.setAssignedTo(agent);
            ticket.setStatus(TicketStatus.ASSIGNED);
        } else if (currentStatus == TicketStatus.ASSIGNED) {
            ticket.setAssignedTo(agent);
        } else {
            throw new RuntimeException("Bu statüdeki ticket yeniden atanamaz: " + currentStatus);
        }

        return ticketRepository.save(ticket);
    }

    @Override
    @Transactional
    public Ticket changeStatus(Long ticketId, ChangeStatusRequest request) {
        Ticket ticket = getTicketById(ticketId);
        TicketStatus currentStatus = ticket.getStatus();
        TicketStatus newStatus = request.getNewStatus();

        if (currentStatus == newStatus) {
            throw new RuntimeException("Ticket zaten bu statüde: " + currentStatus);
        }

        Set<TicketStatus> allowed = VALID_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new RuntimeException("Geçersiz statü geçişi: " + currentStatus + " -> " + newStatus);
        }

        if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        }

        ticket.setStatus(newStatus);
        return ticketRepository.save(ticket);
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

    private String generateTicketNumber() {
        return "TK-" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }
}

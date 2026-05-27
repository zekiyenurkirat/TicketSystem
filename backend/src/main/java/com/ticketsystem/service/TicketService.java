package com.ticketsystem.service;

import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.enums.Priority;
import com.ticketsystem.entity.enums.TicketStatus;

import java.util.List;

/** Ticket yönetimi iş kurallarını tanımlar. */
public interface TicketService {

    /** Yeni ticket oluşturur ve kaydeder. */
    Ticket createTicket(Long createdById, String title, String description, Priority priority);

    /** ID ile ticket getirir. */
    Ticket getTicketById(Long id);

    /** Ticket numarasıyla ticket getirir. */
    Ticket getTicketByTicketNumber(String ticketNumber);

    /** Ticket'ı belirtilen agent'a atar. */
    Ticket assignTicket(Long ticketId, Long agentId);

    /** Ticket statüsünü değiştirir. */
    Ticket changeStatus(Long ticketId, TicketStatus newStatus);

    /** Belirtilen statüdeki ticket'ları getirir. */
    List<Ticket> getTicketsByStatus(TicketStatus status);


    /** Belirtilen öncelikteki ticket'ları getirir. */
    List<Ticket> getTicketsByPriority(Priority priority);

    /** Belirtilen kullanıcının oluşturduğu ticket'ları getirir. */
    List<Ticket> getTicketsByCreatedBy(Long userId);

    /** Belirtilen agent'a atanmış ticket'ları getirir. */
    List<Ticket> getTicketsByAssignedTo(Long agentId);

    /** Henüz atanmamış ticket'ları getirir. */
    List<Ticket> getUnassignedTickets();

    /** Belirtilen statü ve öncelik kombinasyonundaki ticket'ları getirir. */
    List<Ticket> getTicketsByStatusAndPriority(TicketStatus status, Priority priority);
}

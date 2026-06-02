package com.ticketsystem.repository;

import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.Priority;
import com.ticketsystem.entity.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Ticket veritabanı erişim arayüzü. */
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /** Ticket numarasına göre arar. Örn: TK-0001 */
    Optional<Ticket> findByTicketNumber(String ticketNumber);

    /** Belirtilen statüdeki ticket'ları getirir. */
    List<Ticket> findByStatus(TicketStatus status);

    /** Belirtilen öncelikteki ticket'ları getirir. */
    List<Ticket> findByPriority(Priority priority);

    /** Belirli bir kullanıcının oluşturduğu ticket'ları getirir. */
    List<Ticket> findByCreatedBy(User createdBy);

    /** Belirli bir agent'a atanmış ticket'ları getirir. */
    List<Ticket> findByAssignedTo(User assignedTo);

    /** Henüz hiçbir agent'a atanmamış ticket'ları getirir. */
    List<Ticket> findByAssignedToIsNull();

    /** Belirtilen statü ve öncelik kombinasyonuna göre ticket'ları getirir. */
    List<Ticket> findByStatusAndPriority(TicketStatus status, Priority priority);

    /** Verilen statü kümesi dışındaki (terminal olmayan) tüm ticket'ları getirir. */
    List<Ticket> findByStatusNotIn(Collection<TicketStatus> terminalStatuses);
}

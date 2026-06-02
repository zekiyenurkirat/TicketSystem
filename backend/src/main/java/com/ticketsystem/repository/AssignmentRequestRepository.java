package com.ticketsystem.repository;

import com.ticketsystem.entity.AssignmentRequest;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.AssignmentRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** AssignmentRequest veritabanı erişim arayüzü. */
public interface AssignmentRequestRepository extends JpaRepository<AssignmentRequest, Long> {

    /** Aynı agent, ticket ve statü kombinasyonunda istek var mı kontrol eder. */
    boolean existsByRequestedByAndTicketAndStatus(
            User requestedBy, Ticket ticket, AssignmentRequestStatus status);

    /** Belirtilen statüdeki tüm istekleri getirir. */
    List<AssignmentRequest> findByStatus(AssignmentRequestStatus status);

    /** Belirtilen ticket'a ait, belirtilen statüdeki istekleri getirir. */
    List<AssignmentRequest> findByTicketAndStatus(Ticket ticket, AssignmentRequestStatus status);
}

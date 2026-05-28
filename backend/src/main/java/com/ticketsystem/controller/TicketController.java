package com.ticketsystem.controller;

import com.ticketsystem.core.response.ApiResponse;
import com.ticketsystem.dto.request.AssignTicketRequest;
import com.ticketsystem.dto.request.ChangeStatusRequest;
import com.ticketsystem.dto.request.CreateTicketRequest;
import com.ticketsystem.dto.response.TicketResponse;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.enums.Priority;
import com.ticketsystem.entity.enums.TicketStatus;
import com.ticketsystem.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Ticket yönetimi HTTP isteklerini karşılar. */
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** Yeni ticket oluşturur. */
    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @RequestBody @Valid CreateTicketRequest request) {
        Ticket ticket = ticketService.createTicket(request);
        TicketResponse response = TicketResponse.from(ticket);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Ticket başarıyla oluşturuldu."));
    }

    /** ID ile ticket getirir. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketById(@PathVariable Long id) {
        Ticket ticket = ticketService.getTicketById(id);
        TicketResponse response = TicketResponse.from(ticket);
        return ResponseEntity.ok(ApiResponse.success(response, "Ticket getirildi."));
    }

    /** Ticket numarası ile ticket getirir. */
    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketByTicketNumber(
            @PathVariable String ticketNumber) {
        Ticket ticket = ticketService.getTicketByTicketNumber(ticketNumber);
        TicketResponse response = TicketResponse.from(ticket);
        return ResponseEntity.ok(ApiResponse.success(response, "Ticket getirildi."));
    }

    /** Ticket'ı belirtilen agent'a atar. */
    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(
            @PathVariable Long id,
            @RequestBody @Valid AssignTicketRequest request) {
        Ticket ticket = ticketService.assignTicket(id, request);
        TicketResponse response = TicketResponse.from(ticket);
        return ResponseEntity.ok(ApiResponse.success(response, "Ticket atandı."));
    }

    /** Ticket statüsünü günceller. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TicketResponse>> changeStatus(
            @PathVariable Long id,
            @RequestBody @Valid ChangeStatusRequest request) {
        Ticket ticket = ticketService.changeStatus(id, request);
        TicketResponse response = TicketResponse.from(ticket);
        return ResponseEntity.ok(ApiResponse.success(response, "Ticket statüsü güncellendi."));
    }

    /** Belirtilen statüdeki ticket'ları getirir. */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByStatus(
            @PathVariable TicketStatus status) {
        List<TicketResponse> responses = ticketService.getTicketsByStatus(status)
                .stream()
                .map(TicketResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Ticket listesi getirildi."));
    }

    /** Belirtilen öncelikteki ticket'ları getirir. */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByPriority(
            @PathVariable Priority priority) {
        List<TicketResponse> responses = ticketService.getTicketsByPriority(priority)
                .stream()
                .map(TicketResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Ticket listesi getirildi."));
    }

    /** Belirtilen kullanıcının oluşturduğu ticket'ları getirir. */
    @GetMapping("/created-by/{userId}")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByCreatedBy(
            @PathVariable Long userId) {
        List<TicketResponse> responses = ticketService.getTicketsByCreatedBy(userId)
                .stream()
                .map(TicketResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Ticket listesi getirildi."));
    }

    /** Belirtilen agent'a atanmış ticket'ları getirir. */
    @GetMapping("/assigned-to/{agentId}")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByAssignedTo(
            @PathVariable Long agentId) {
        List<TicketResponse> responses = ticketService.getTicketsByAssignedTo(agentId)
                .stream()
                .map(TicketResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Ticket listesi getirildi."));
    }

    /** Henüz atanmamış ticket'ları getirir. */
    @GetMapping("/unassigned")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getUnassignedTickets() {
        List<TicketResponse> responses = ticketService.getUnassignedTickets()
                .stream()
                .map(TicketResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Atanmamış ticket listesi getirildi."));
    }

    /** Statü ve öncelik kombinasyonuna göre ticket'ları getirir. */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByStatusAndPriority(
            @RequestParam TicketStatus status,
            @RequestParam Priority priority) {
        List<TicketResponse> responses = ticketService.getTicketsByStatusAndPriority(status, priority)
                .stream()
                .map(TicketResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Filtrelenmiş ticket listesi getirildi."));
    }
}

package com.ticketsystem.controller;

import com.ticketsystem.core.response.ApiResponse;
import com.ticketsystem.dto.request.AssignTicketRequest;
import com.ticketsystem.dto.request.ChangeStatusRequest;
import com.ticketsystem.dto.request.CreateTicketRequest;
import com.ticketsystem.dto.request.PriorityReviewRequest;
import com.ticketsystem.dto.response.TicketResponse;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.enums.Priority;
import com.ticketsystem.entity.enums.TicketStatus;
import com.ticketsystem.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Ticket yönetimi HTTP isteklerini karşılar. */
@Tag(name = "Ticket Yönetimi", description = "Ticket oluşturma, atama, statü güncelleme ve sorgulama işlemleri")
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** Yeni ticket oluşturur. */
    @Operation(summary = "Yeni ticket oluşturur",
               description = "Sistemde yeni bir destek talebi oluşturur. TK-XXXXXXXX formatında ticketNumber atanır, statü NEW olarak belirlenir ve SLA kuralına göre dueDate hesaplanır.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Ticket başarıyla oluşturuldu."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validasyon hatası veya geçersiz istek."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen kullanıcı bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @RequestBody @Valid CreateTicketRequest request) {
        Ticket ticket = ticketService.createTicket(request);
        TicketResponse response = TicketResponse.from(ticket);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Ticket başarıyla oluşturuldu."));
    }

    /** ID ile ticket getirir. */
    @Operation(summary = "ID ile ticket getirir",
               description = "Belirtilen ID'ye sahip ticket'ı döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ID'ye sahip ticket bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketById(@PathVariable Long id) {
        Ticket ticket = ticketService.getTicketById(id);
        TicketResponse response = TicketResponse.from(ticket);
        return ResponseEntity.ok(ApiResponse.success(response, "Ticket getirildi."));
    }

    /** Ticket numarası ile ticket getirir. */
    @Operation(summary = "Ticket numarası ile ticket getirir",
               description = "Belirtilen TK-XXXXXXXX formatındaki ticket numarasına sahip ticket'ı döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ticket numarasına sahip ticket bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketByTicketNumber(
            @PathVariable String ticketNumber) {
        Ticket ticket = ticketService.getTicketByTicketNumber(ticketNumber);
        TicketResponse response = TicketResponse.from(ticket);
        return ResponseEntity.ok(ApiResponse.success(response, "Ticket getirildi."));
    }

    /** Ticket'ı agent'a atar. */
    @Operation(summary = "Ticket'ı agent'a atar",
               description = "Belirtilen ticket'ı bir agent'a atar. Agent aktif ve AGENT rolünde olmalıdır. Ticket statüsü NEW ise ASSIGNED, zaten ASSIGNED ise assignedTo güncellenir.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket başarıyla atandı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "İş kuralı ihlali veya geçersiz istek."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ticket veya agent bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(
            @PathVariable Long id,
            @RequestBody @Valid AssignTicketRequest request) {
        Ticket ticket = ticketService.assignTicket(id, request);
        TicketResponse response = TicketResponse.from(ticket);
        return ResponseEntity.ok(ApiResponse.success(response, "Ticket atandı."));
    }

    /** Ticket önceliğini gözden geçirir ve günceller. */
    @Operation(summary = "Ticket önceliğini gözden geçirir",
               description = "AGENT veya MANAGER tarafından ticket'ın aktif önceliği güncellenir, dueDate yeniden hesaplanır ve review meta bilgileri kaydedilir.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket önceliği başarıyla güncellendi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validasyon hatası veya geçersiz istek."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ID'ye sahip ticket bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @PatchMapping("/{id}/priority-review")
    public ResponseEntity<ApiResponse<TicketResponse>> reviewPriority(
            @PathVariable Long id,
            @RequestBody @Valid PriorityReviewRequest request) {
        Ticket ticket = ticketService.reviewPriority(id, request);
        TicketResponse response = TicketResponse.from(ticket);
        return ResponseEntity.ok(ApiResponse.success(response, "Ticket önceliği değerlendirildi."));
    }

    /** Ticket statüsünü günceller. */
    @Operation(summary = "Ticket statüsünü günceller",
               description = "Ticket statüsünü geçerli geçiş kurallarına göre günceller. İzin verilmeyen geçişler reddedilir.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket statüsü başarıyla güncellendi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Geçersiz statü geçişi veya validasyon hatası."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ID'ye sahip ticket bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TicketResponse>> changeStatus(
            @PathVariable Long id,
            @RequestBody @Valid ChangeStatusRequest request) {
        Ticket ticket = ticketService.changeStatus(id, request);
        TicketResponse response = TicketResponse.from(ticket);
        return ResponseEntity.ok(ApiResponse.success(response, "Ticket statüsü güncellendi."));
    }

    /** Statüye göre ticket listesi getirir. */
    @Operation(summary = "Statüye göre ticket listesi getirir",
               description = "Belirtilen statüdeki tüm ticket'ları döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket listesi başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByStatus(
            @PathVariable TicketStatus status) {
        List<TicketResponse> responses = ticketService.getTicketsByStatus(status)
                .stream()
                .map(TicketResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Ticket listesi getirildi."));
    }

    /** Önceliğe göre ticket listesi getirir. */
    @Operation(summary = "Önceliğe göre ticket listesi getirir",
               description = "Belirtilen öncelik seviyesindeki tüm ticket'ları döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket listesi başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/priority/{priority}")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByPriority(
            @PathVariable Priority priority) {
        List<TicketResponse> responses = ticketService.getTicketsByPriority(priority)
                .stream()
                .map(TicketResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Ticket listesi getirildi."));
    }

    /** Oluşturan kullanıcıya göre ticket listesi getirir. */
    @Operation(summary = "Oluşturan kullanıcıya göre ticket listesi getirir",
               description = "Belirtilen kullanıcının oluşturduğu tüm ticket'ları döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket listesi başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ID'ye sahip kullanıcı bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/created-by/{userId}")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByCreatedBy(
            @PathVariable Long userId) {
        List<TicketResponse> responses = ticketService.getTicketsByCreatedBy(userId)
                .stream()
                .map(TicketResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Ticket listesi getirildi."));
    }

    /** Atanmış agent'a göre ticket listesi getirir. */
    @Operation(summary = "Atanmış agent'a göre ticket listesi getirir",
               description = "Belirtilen agent'a atanmış tüm ticket'ları döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket listesi başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ID'ye sahip agent bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/assigned-to/{agentId}")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByAssignedTo(
            @PathVariable Long agentId) {
        List<TicketResponse> responses = ticketService.getTicketsByAssignedTo(agentId)
                .stream()
                .map(TicketResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Ticket listesi getirildi."));
    }

    /** Atanmamış ticket'ları getirir. */
    @Operation(summary = "Atanmamış ticket'ları getirir",
               description = "Henüz herhangi bir agent'a atanmamış tüm ticket'ları döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Atanmamış ticket listesi başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/unassigned")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getUnassignedTickets() {
        List<TicketResponse> responses = ticketService.getUnassignedTickets()
                .stream()
                .map(TicketResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Atanmamış ticket listesi getirildi."));
    }

    /** Statü ve öncelik filtresiyle ticket listesi getirir. */
    @Operation(summary = "Statü ve öncelik filtresiyle ticket listesi getirir",
               description = "Belirtilen statü ve öncelik kombinasyonuna uyan tüm ticket'ları döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Filtrelenmiş ticket listesi başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
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

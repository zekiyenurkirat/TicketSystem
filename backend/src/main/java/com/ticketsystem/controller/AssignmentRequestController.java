package com.ticketsystem.controller;

import com.ticketsystem.core.response.ApiResponse;
import com.ticketsystem.dto.request.CreateAssignmentRequestRequest;
import com.ticketsystem.dto.response.AssignmentRequestResponse;
import com.ticketsystem.entity.enums.AssignmentRequestStatus;
import com.ticketsystem.service.AssignmentRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Agent atama istekleri HTTP isteklerini karşılar. */
@Tag(name = "Atama İstekleri", description = "Agent'ların ticket atama isteklerini yönetme")
@RestController
@RequestMapping("/api/v1/assignment-requests")
public class AssignmentRequestController {

    private final AssignmentRequestService assignmentRequestService;

    public AssignmentRequestController(AssignmentRequestService assignmentRequestService) {
        this.assignmentRequestService = assignmentRequestService;
    }

    /** Oturum açmış agent için yeni atama isteği oluşturur. */
    @Operation(summary = "Atama isteği oluştur",
               description = "AGENT rolü için. Yalnızca NEW statüsünde ve atanmamış ticket'lar için istek açılabilir.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Atama isteği başarıyla oluşturuldu."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Geçersiz istek verisi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "İş kuralı ihlali.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<AssignmentRequestResponse>> createRequest(
            @Valid @RequestBody CreateAssignmentRequestRequest request) {
        AssignmentRequestResponse response = assignmentRequestService.createRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Atama isteği oluşturuldu."));
    }

    /** Belirtilen statüdeki atama isteklerini listeler. Varsayılan: PENDING. */
    @Operation(summary = "Atama isteklerini listele",
               description = "MANAGER rolü için. status parametresi verilmezse PENDING istekler döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "İstekler başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<AssignmentRequestResponse>>> getRequests(
            @RequestParam(defaultValue = "PENDING") AssignmentRequestStatus status) {
        List<AssignmentRequestResponse> responses = assignmentRequestService.getRequests(status);
        return ResponseEntity.ok(ApiResponse.success(responses, "Atama istekleri getirildi."));
    }

    /** Belirtilen atama isteğini onaylar; ticket ilgili agent'a atanır. */
    @Operation(summary = "Atama isteğini onayla",
               description = "MANAGER rolü için. Ticket agent'a atanır; aynı ticket'ın diğer PENDING istekleri otomatik reddedilir.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "İstek onaylandı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Atama isteği bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "İstek zaten işlenmiş.")
    })
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<AssignmentRequestResponse>> approveRequest(
            @PathVariable Long id) {
        AssignmentRequestResponse response = assignmentRequestService.approveRequest(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Atama isteği onaylandı."));
    }

    /** Belirtilen atama isteğini reddeder; ticket'a dokunulmaz. */
    @Operation(summary = "Atama isteğini reddet",
               description = "MANAGER rolü için. Ticket'a dokunulmaz; ilgili agent'a bildirim gönderilir.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "İstek reddedildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Atama isteği bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "İstek zaten işlenmiş.")
    })
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<AssignmentRequestResponse>> rejectRequest(
            @PathVariable Long id) {
        AssignmentRequestResponse response = assignmentRequestService.rejectRequest(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Atama isteği reddedildi."));
    }
}

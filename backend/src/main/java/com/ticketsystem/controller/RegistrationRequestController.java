package com.ticketsystem.controller;

import com.ticketsystem.core.response.ApiResponse;
import com.ticketsystem.dto.request.CreateRegistrationRequestRequest;
import com.ticketsystem.dto.request.RejectRegistrationRequestRequest;
import com.ticketsystem.dto.response.RegistrationRequestResponse;
import com.ticketsystem.entity.enums.RegistrationRequestStatus;
import com.ticketsystem.service.RegistrationRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Kayıt talebi HTTP isteklerini karşılar. */
@Tag(name = "Kayıt Talepleri", description = "Kullanıcı kayıt talebi oluşturma, listeleme, onaylama ve reddetme işlemleri")
@RestController
@RequestMapping("/api/v1/registration-requests")
public class RegistrationRequestController {

    private final RegistrationRequestService registrationRequestService;

    public RegistrationRequestController(RegistrationRequestService registrationRequestService) {
        this.registrationRequestService = registrationRequestService;
    }

    /** Yeni kayıt talebi oluşturur. Public endpoint; yalnızca CUSTOMER kaydı için kullanılır. */
    @Operation(summary = "Yeni kayıt talebi oluşturur",
               description = "Sisteme üye olmak isteyen kullanıcı için PENDING statüsünde kayıt talebi oluşturur. Manager onayı gereklidir.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Kayıt talebi başarıyla oluşturuldu."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validasyon hatası veya geçersiz istek."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Bu e-posta için zaten kayıtlı kullanıcı veya bekleyen talep mevcut.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<RegistrationRequestResponse>> createRegistrationRequest(
            @RequestBody @Valid CreateRegistrationRequestRequest request) {
        RegistrationRequestResponse response = registrationRequestService.createRegistrationRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Kayıt talebiniz alındı. Manager onayı bekleniyor."));
    }

    /** Kayıt taleplerini statüye göre listeler. Yalnızca MANAGER erişebilir. */
    @Operation(summary = "Kayıt taleplerini listeler",
               description = "Belirtilen statüdeki kayıt taleplerini döner. status parametresi verilmezse PENDING döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<RegistrationRequestResponse>>> getRegistrationRequests(
            @RequestParam(defaultValue = "PENDING") RegistrationRequestStatus status) {
        List<RegistrationRequestResponse> responses = registrationRequestService.getRegistrationRequests(status);
        return ResponseEntity.ok(ApiResponse.success(responses, "Kayıt talepleri getirildi."));
    }

    /** Kayıt talebini onaylar ve kullanıcı hesabını oluşturur. Yalnızca MANAGER erişebilir. */
    @Operation(summary = "Kayıt talebini onaylar",
               description = "PENDING statüsündeki kayıt talebini onaylar; kullanıcı hesabı active=true olarak oluşturulur.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Kayıt talebi onaylandı, kullanıcı hesabı oluşturuldu."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Talep zaten işlenmiş."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Kayıt talebi bulunamadı.")
    })
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<RegistrationRequestResponse>> approveRegistrationRequest(
            @PathVariable Long id) {
        RegistrationRequestResponse response = registrationRequestService.approveRegistrationRequest(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Kayıt talebi onaylandı, kullanıcı hesabı oluşturuldu."));
    }

    /** Kayıt talebini reddeder. Kullanıcı hesabı oluşturulmaz. Yalnızca MANAGER erişebilir. */
    @Operation(summary = "Kayıt talebini reddeder",
               description = "PENDING statüsündeki kayıt talebini reddeder. Kullanıcı hesabı oluşturulmaz.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Kayıt talebi reddedildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Talep zaten işlenmiş."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Kayıt talebi bulunamadı.")
    })
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<RegistrationRequestResponse>> rejectRegistrationRequest(
            @PathVariable Long id,
            @RequestBody(required = false) RejectRegistrationRequestRequest request) {
        String note = request != null ? request.getNote() : null;
        RegistrationRequestResponse response = registrationRequestService.rejectRegistrationRequest(id, note);
        return ResponseEntity.ok(ApiResponse.success(response, "Kayıt talebi reddedildi."));
    }
}

package com.ticketsystem.controller;

import com.ticketsystem.core.response.ApiResponse;
import com.ticketsystem.dto.request.SaveAttachmentRequest;
import com.ticketsystem.dto.response.AttachmentResponse;
import com.ticketsystem.entity.Attachment;
import com.ticketsystem.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Dosya eki yönetimi HTTP isteklerini karşılar. */
@Tag(name = "Dosya Eki Yönetimi", description = "Ticket dosya eki meta bilgilerini kaydetme ve sorgulama işlemleri")
@RestController
@RequestMapping("/api/v1/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /** Dosya eki meta bilgilerini kaydeder. */
    @Operation(summary = "Dosya eki meta bilgilerini kaydeder",
               description = "Bir ticket'a ait dosya meta bilgilerini kaydeder. Gerçek dosya içeriği kaydedilmez; yalnızca meta bilgi kaydı oluşturulur.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Dosya kaydı başarıyla oluşturuldu."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validasyon hatası veya geçersiz istek."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ticket veya kullanıcı bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<AttachmentResponse>> saveAttachmentRecord(
            @RequestBody @Valid SaveAttachmentRequest request) {
        Attachment attachment = attachmentService.saveAttachmentRecord(request);
        AttachmentResponse response = AttachmentResponse.from(attachment);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Dosya kaydı başarıyla oluşturuldu."));
    }

    /** Ticket'a ait dosya kayıtlarını getirir. */
    @Operation(summary = "Ticket'a ait dosya kayıtlarını getirir",
               description = "Belirtilen ticket'a yüklenmiş tüm dosyaların meta bilgilerini döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dosya listesi başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ID'ye sahip ticket bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachmentsByTicket(
            @PathVariable Long ticketId) {
        List<AttachmentResponse> responses = attachmentService.getAttachmentsByTicket(ticketId)
                .stream()
                .map(AttachmentResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Dosya listesi getirildi."));
    }

    /** Yükleyen kullanıcıya göre dosya kayıtlarını getirir. */
    @Operation(summary = "Yükleyen kullanıcıya göre dosya kayıtlarını getirir",
               description = "Belirtilen kullanıcının yüklediği tüm dosyaların meta bilgilerini döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dosya listesi başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ID'ye sahip kullanıcı bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/uploader/{userId}")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachmentsByUploadedBy(
            @PathVariable Long userId) {
        List<AttachmentResponse> responses = attachmentService.getAttachmentsByUploadedBy(userId)
                .stream()
                .map(AttachmentResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Dosya listesi getirildi."));
    }
}

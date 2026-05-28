package com.ticketsystem.controller;

import com.ticketsystem.core.response.ApiResponse;
import com.ticketsystem.dto.request.SaveAttachmentRequest;
import com.ticketsystem.dto.response.AttachmentResponse;
import com.ticketsystem.entity.Attachment;
import com.ticketsystem.service.AttachmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Dosya eki yönetimi HTTP isteklerini karşılar. */
@RestController
@RequestMapping("/api/v1/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /** Dosya meta bilgilerini kaydeder. */
    @PostMapping
    public ResponseEntity<ApiResponse<AttachmentResponse>> saveAttachmentRecord(
            @RequestBody @Valid SaveAttachmentRequest request) {
        Attachment attachment = attachmentService.saveAttachmentRecord(request);
        AttachmentResponse response = AttachmentResponse.from(attachment);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Dosya kaydı başarıyla oluşturuldu."));
    }

    /** Ticket'a ait dosya kayıtlarını getirir. */
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachmentsByTicket(
            @PathVariable Long ticketId) {
        List<AttachmentResponse> responses = attachmentService.getAttachmentsByTicket(ticketId)
                .stream()
                .map(AttachmentResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Dosya listesi getirildi."));
    }

    /** Belirtilen kullanıcının yüklediği dosya kayıtlarını getirir. */
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

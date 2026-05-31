package com.ticketsystem.controller;

import com.ticketsystem.core.response.ApiResponse;
import com.ticketsystem.dto.response.AttachmentDownloadResult;
import com.ticketsystem.dto.response.AttachmentResponse;
import com.ticketsystem.entity.Attachment;
import com.ticketsystem.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Dosya eki yönetimi HTTP isteklerini karşılar. */
@Tag(name = "Dosya Eki Yönetimi", description = "Ticket dosya eki yükleme, indirme ve sorgulama işlemleri")
@RestController
@RequestMapping("/api/v1/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /** Gerçek dosyayı ticket'a yükler. */
    @Operation(summary = "Dosyayı ticket'a yükler",
               description = "multipart/form-data formatında gerçek dosya yükler. "
                           + "Geçerli JWT token gereklidir. "
                           + "İzin verilen formatlar: pdf, doc, docx, png, jpg, jpeg, txt. "
                           + "Maksimum dosya boyutu: 10 MB. "
                           + "uploadedBy token'dan belirlenir; client'tan alınmaz.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Dosya başarıyla yüklendi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Geçersiz dosya: boş, izin verilmeyen uzantı, MIME uyumsuzluğu veya boyut aşımı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Başkasının ticket'ına erişim yetkisi yok."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ticket bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
            @RequestParam("ticketId") Long ticketId,
            @RequestParam("file") MultipartFile file) {
        Attachment attachment = attachmentService.uploadAttachment(ticketId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AttachmentResponse.from(attachment), "Dosya başarıyla yüklendi."));
    }

    /** Yüklenmiş dosyayı güvenli şekilde indirir. */
    @Operation(summary = "Dosyayı indirir",
               description = "Attachment ID ile yüklenmiş gerçek dosyayı indirir. "
                           + "Geçerli JWT token gereklidir. "
                           + "CUSTOMER yalnızca kendi ticket'ının dosyasını indirebilir. "
                           + "AGENT ve MANAGER tüm dosyalara erişebilir.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dosya başarıyla indirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Bu dosyaya erişim yetkiniz yok."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Attachment veya fiziksel dosya bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) {
        AttachmentDownloadResult result = attachmentService.downloadAttachment(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.fileName() + "\"")
                .contentType(MediaType.parseMediaType(result.fileType()))
                .contentLength(result.fileSize())
                .body(result.resource());
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

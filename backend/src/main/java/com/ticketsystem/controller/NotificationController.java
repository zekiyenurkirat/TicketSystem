package com.ticketsystem.controller;

import com.ticketsystem.core.response.ApiResponse;
import com.ticketsystem.dto.response.NotificationResponse;
import com.ticketsystem.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Bildirim yönetimi HTTP isteklerini karşılar. */
@Tag(name = "Bildirim Yönetimi", description = "Kullanıcı bildirimlerini listeleme ve okundu işaretleme")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** Oturum açmış kullanıcının son 50 bildirimini getirir. */
    @Operation(summary = "Kendi bildirimlerimi getir",
               description = "Oturum açmış kullanıcının son 50 bildirimini yeniden eskiye sıralı döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bildirimler başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        List<NotificationResponse> notifications = notificationService.getMyNotifications();
        return ResponseEntity.ok(ApiResponse.success(notifications, "Bildirimler getirildi."));
    }

    /** Belirtilen bildirimi okundu olarak işaretler. */
    @Operation(summary = "Bildirimi okundu işaretle",
               description = "Belirtilen bildirimi seen=true yapar. Bildirim başka bir kullanıcıya aitse 403 döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bildirim okundu olarak işaretlendi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Bu bildirimi işaretleme yetkiniz yok."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bildirim bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @PatchMapping("/{id}/seen")
    public ResponseEntity<ApiResponse<Void>> markAsSeen(@PathVariable Long id) {
        notificationService.markAsSeen(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Bildirim okundu olarak işaretlendi."));
    }

    /** Oturum açmış kullanıcının tüm okunmamış bildirimlerini okundu yapar. */
    @Operation(summary = "Tüm bildirimleri okundu işaretle",
               description = "Oturum açmış kullanıcının tüm okunmamış bildirimlerini seen=true yapar.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tüm bildirimler okundu olarak işaretlendi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @PatchMapping("/seen-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsSeen() {
        notificationService.markAllAsSeen();
        return ResponseEntity.ok(ApiResponse.success(null, "Tüm bildirimler okundu olarak işaretlendi."));
    }
}

package com.ticketsystem.controller;

import com.ticketsystem.core.response.ApiResponse;
import com.ticketsystem.dto.request.AddCommentRequest;
import com.ticketsystem.dto.response.CommentResponse;
import com.ticketsystem.entity.Comment;
import com.ticketsystem.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Yorum yönetimi HTTP isteklerini karşılar. */
@Tag(name = "Yorum Yönetimi", description = "Ticket yorumlarını ekleme ve sorgulama işlemleri")
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /** Ticket'a yorum ekler. */
    @Operation(summary = "Ticket'a yorum ekler",
               description = "Belirtilen ticket'a yorum ekler. CUSTOMER rolündeki kullanıcılar yalnızca EXTERNAL yorum ekleyebilir; INTERNAL yorum yalnızca AGENT ve MANAGER tarafından eklenebilir.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Yorum başarıyla eklendi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "İş kuralı ihlali veya validasyon hatası."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ticket veya kullanıcı bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @RequestBody @Valid AddCommentRequest request) {
        Comment comment = commentService.addComment(request);
        CommentResponse response = CommentResponse.from(comment);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Yorum başarıyla eklendi."));
    }

    /** Ticket yorumlarını getirir. */
    @Operation(summary = "Ticket yorumlarını getirir",
               description = "Belirtilen ticket'a ait yorumları döner. requesterId, yorumları isteyen kullanıcıyı temsil eder. CUSTOMER rolündeki kullanıcılar yalnızca EXTERNAL yorumları görür; AGENT ve MANAGER tüm yorumları görür.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Yorumlar başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ticket veya kullanıcı bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsForTicket(
            @PathVariable Long ticketId,
            @RequestParam Long requesterId) {
        List<CommentResponse> responses = commentService.getCommentsForTicket(ticketId, requesterId)
                .stream()
                .map(CommentResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Yorumlar getirildi."));
    }

    /** Yazara göre yorumları getirir. */
    @Operation(summary = "Yazara göre yorumları getirir",
               description = "Belirtilen kullanıcının yazdığı tüm yorumları döner.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Yorumlar başarıyla getirildi."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Belirtilen ID'ye sahip kullanıcı bulunamadı."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Beklenmeyen bir hata oluştu.")
    })
    @GetMapping("/author/{authorId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByAuthor(
            @PathVariable Long authorId) {
        List<CommentResponse> responses = commentService.getCommentsByAuthor(authorId)
                .stream()
                .map(CommentResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses, "Yorumlar getirildi."));
    }
}

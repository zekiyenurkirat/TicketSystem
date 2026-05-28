package com.ticketsystem.controller;

import com.ticketsystem.core.response.ApiResponse;
import com.ticketsystem.dto.request.AddCommentRequest;
import com.ticketsystem.dto.response.CommentResponse;
import com.ticketsystem.entity.Comment;
import com.ticketsystem.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Yorum yönetimi HTTP isteklerini karşılar. */
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /** Ticket'a yorum ekler. */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @RequestBody @Valid AddCommentRequest request) {
        Comment comment = commentService.addComment(request);
        CommentResponse response = CommentResponse.from(comment);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Yorum başarıyla eklendi."));
    }

    /** Ticket yorumlarını requester rolüne göre filtreli getirir. */
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

    /** Belirtilen kullanıcının yazdığı yorumları getirir. */
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

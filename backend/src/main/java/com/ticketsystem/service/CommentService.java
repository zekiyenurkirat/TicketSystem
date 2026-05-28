package com.ticketsystem.service;

import com.ticketsystem.dto.request.AddCommentRequest;
import com.ticketsystem.entity.Comment;

import java.util.List;

/** Yorum yönetimi iş kurallarını tanımlar. */
public interface CommentService {

    /** Ticket'a yorum ekler. */
    Comment addComment(AddCommentRequest request);

    /** Ticket yorumlarını requester rolüne göre filtreli getirir. */
    List<Comment> getCommentsForTicket(Long ticketId, Long requesterId);

    /** Belirtilen kullanıcının yazdığı yorumları getirir. */
    List<Comment> getCommentsByAuthor(Long authorId);
}

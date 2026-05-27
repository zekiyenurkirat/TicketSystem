package com.ticketsystem.service;

import com.ticketsystem.entity.Comment;
import com.ticketsystem.entity.enums.CommentType;

import java.util.List;

/** Yorum yönetimi iş kurallarını tanımlar. */
public interface CommentService {

    /** Ticket'a yorum ekler. */
    Comment addComment(Long ticketId, Long authorId, String content, CommentType type);

    /** Ticket yorumlarını requester rolüne göre filtreli getirir. */
    List<Comment> getCommentsForTicket(Long ticketId, Long requesterId);

    /** Belirtilen kullanıcının yazdığı yorumları getirir. */
    List<Comment> getCommentsByAuthor(Long authorId);
}

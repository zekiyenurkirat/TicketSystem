package com.ticketsystem.dto.response;

import com.ticketsystem.entity.Comment;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.CommentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Yorum bilgilerini dışarıya taşıyan yanıt nesnesi. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long ticketId;
    private Long authorId;
    private String authorFullName;
    private String content;
    private CommentType type;
    private LocalDateTime createdAt;

    /** {@link Comment} entity'sinden {@link CommentResponse} üretir. */
    public static CommentResponse from(Comment comment) {
        Long ticketId = comment.getTicket() != null ? comment.getTicket().getId() : null;

        User author = comment.getAuthor();
        Long authorId = author != null ? author.getId() : null;
        String authorFullName = author != null
                ? author.getFirstName() + " " + author.getLastName()
                : null;

        return new CommentResponse(
                comment.getId(),
                ticketId,
                authorId,
                authorFullName,
                comment.getContent(),
                comment.getType(),
                comment.getCreatedAt()
        );
    }
}

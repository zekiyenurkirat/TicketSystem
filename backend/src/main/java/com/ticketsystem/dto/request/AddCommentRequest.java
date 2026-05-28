package com.ticketsystem.dto.request;

import com.ticketsystem.entity.enums.CommentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Ticket'a yorum ekleme isteği için veri taşıyıcı. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentRequest {

    @NotNull
    private Long ticketId;

    @NotNull
    private Long authorId;

    @NotBlank
    private String content;

    @NotNull
    private CommentType type;
}

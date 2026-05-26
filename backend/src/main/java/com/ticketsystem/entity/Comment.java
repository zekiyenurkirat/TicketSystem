package com.ticketsystem.entity;

import com.ticketsystem.entity.enums.CommentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bir ticket'a yapılan yorumu temsil eden entity.
 * "comments" tablosuna karşılık gelir.
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
public class Comment extends BaseEntity {

    /** Yorum içeriği. */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Bu yorumun ait olduğu ticket. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /** Yorumu yazan kullanıcı. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /** Yorumun görünürlük tipi (INTERNAL veya EXTERNAL). */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CommentType type;
}

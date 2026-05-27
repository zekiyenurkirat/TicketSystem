package com.ticketsystem.repository;

import com.ticketsystem.entity.Comment;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.CommentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Yorum veritabanı erişim arayüzü. */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** Bir ticket'a ait yorumları oluşturulma tarihine göre artan sırada getirir. */
    List<Comment> findByTicketOrderByCreatedAtAsc(Ticket ticket);

    /** Bir ticket'a ait belirli türdeki yorumları getirir. */
    List<Comment> findByTicketAndType(Ticket ticket, CommentType type);

    /** Belirli bir kullanıcının yazdığı yorumları getirir. */
    List<Comment> findByAuthor(User author);
}

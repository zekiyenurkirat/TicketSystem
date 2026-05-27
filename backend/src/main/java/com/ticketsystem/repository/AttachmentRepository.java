package com.ticketsystem.repository;

import com.ticketsystem.entity.Attachment;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Dosya eki veritabanı erişim arayüzü. */
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    /** Bir ticket'a eklenen tüm dosyaları getirir. */
    List<Attachment> findByTicket(Ticket ticket);

    /** Belirli bir kullanıcının yüklediği dosyaları getirir. */
    List<Attachment> findByUploadedBy(User uploadedBy);
}

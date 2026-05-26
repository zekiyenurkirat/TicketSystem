package com.ticketsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bir ticket'a eklenen dosyayı temsil eden entity.
 * "attachments" tablosuna karşılık gelir.
 * Dosyanın kendisi değil, yalnızca meta bilgileri saklanır.
 */
@Entity
@Table(name = "attachments")
@Getter
@Setter
@NoArgsConstructor
public class Attachment extends BaseEntity {

    /** Kullanıcının yüklediği dosyanın orijinal adı. Örn: "ekran-goruntusu.png" */
    @Column(name = "file_name", nullable = false)
    private String fileName;

    /** Dosyanın MIME tipi. Örn: "image/png", "application/pdf", "text/plain" */
    @Column(name = "file_type", nullable = false)
    private String fileType;

    /** Dosyanın sunucudaki kayıt yolu veya erişim URL'i. */
    @Column(name = "file_path", nullable = false)
    private String filePath;

    /** Dosya boyutu (bayt cinsinden). Örn: 204800 → 200 KB */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /** Bu dosyanın ait olduğu ticket. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /** Dosyayı yükleyen kullanıcı. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;
}

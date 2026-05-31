package com.ticketsystem.dto.response;

import org.springframework.core.io.Resource;

/**
 * Dosya indirme işlemi için kaynak ve HTTP header metadata'sını bir arada taşır.
 *
 * <p>Service katmanı hem fiziksel dosyayı ({@link Resource}) hem de
 * Content-Disposition / Content-Type / Content-Length header'ları için
 * gerekli metadata'yı bu record ile döner. Controller HTTP yanıt yapısını
 * bu record üzerinden oluşturur.</p>
 *
 * <p>{@code filePath} bu record'a dahil edilmez; storage yolu client'a hiçbir
 * zaman açıklanmaz.</p>
 */
public record AttachmentDownloadResult(
        Resource resource,
        String fileName,
        String fileType,
        Long fileSize
) {}

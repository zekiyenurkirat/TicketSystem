package com.ticketsystem.service;

import com.ticketsystem.entity.Attachment;

import java.util.List;

/** Dosya eki yönetimi iş kurallarını tanımlar. */
public interface AttachmentService {

    /** Dosya meta bilgilerini kaydeder. */
    Attachment saveAttachmentRecord(Long ticketId, Long uploadedById,
                                    String fileName, String fileType,
                                    String filePath, Long fileSize);

    /** Ticket'a ait dosya kayıtlarını getirir. */
    List<Attachment> getAttachmentsByTicket(Long ticketId);

    /** Belirtilen kullanıcının yüklediği dosya kayıtlarını getirir. */
    List<Attachment> getAttachmentsByUploadedBy(Long userId);
}

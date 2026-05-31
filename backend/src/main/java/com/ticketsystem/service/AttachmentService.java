package com.ticketsystem.service;

import com.ticketsystem.dto.request.SaveAttachmentRequest;
import com.ticketsystem.entity.Attachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Dosya eki yönetimi iş kurallarını tanımlar. */
public interface AttachmentService {

    /** Dosya meta bilgilerini kaydeder. */
    Attachment saveAttachmentRecord(SaveAttachmentRequest request);

    /**
     * Gerçek MultipartFile upload işlemini gerçekleştirir.
     * uploadedById token'dan belirlenir; client'tan alınmaz.
     */
    Attachment uploadAttachment(Long ticketId, MultipartFile file);

    /** Ticket'a ait dosya kayıtlarını getirir. */
    List<Attachment> getAttachmentsByTicket(Long ticketId);

    /** Belirtilen kullanıcının yüklediği dosya kayıtlarını getirir. */
    List<Attachment> getAttachmentsByUploadedBy(Long userId);
}

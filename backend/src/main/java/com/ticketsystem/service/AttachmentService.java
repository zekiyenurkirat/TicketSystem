package com.ticketsystem.service;

import com.ticketsystem.dto.response.AttachmentDownloadResult;
import com.ticketsystem.entity.Attachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Dosya eki yönetimi iş kurallarını tanımlar. */
public interface AttachmentService {

    /**
     * Gerçek MultipartFile upload işlemini gerçekleştirir.
     * uploadedById token'dan belirlenir; client'tan alınmaz.
     */
    Attachment uploadAttachment(Long ticketId, MultipartFile file);

    /**
     * Attachment ID ile yüklenmiş dosyayı güvenli şekilde indirir.
     * CUSTOMER ownership zinciri çalışır; CUSTOMER yalnızca kendi ticket dosyasını indirebilir.
     * AGENT ve MANAGER tüm dosyalara erişebilir.
     */
    AttachmentDownloadResult downloadAttachment(Long attachmentId);

    /** Ticket'a ait dosya kayıtlarını getirir. */
    List<Attachment> getAttachmentsByTicket(Long ticketId);

    /** Belirtilen kullanıcının yüklediği dosya kayıtlarını getirir. */
    List<Attachment> getAttachmentsByUploadedBy(Long userId);
}

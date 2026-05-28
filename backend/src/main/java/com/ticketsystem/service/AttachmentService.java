package com.ticketsystem.service;

import com.ticketsystem.dto.request.SaveAttachmentRequest;
import com.ticketsystem.entity.Attachment;

import java.util.List;

/** Dosya eki yönetimi iş kurallarını tanımlar. */
public interface AttachmentService {

    /** Dosya meta bilgilerini kaydeder. */
    Attachment saveAttachmentRecord(SaveAttachmentRequest request);

    /** Ticket'a ait dosya kayıtlarını getirir. */
    List<Attachment> getAttachmentsByTicket(Long ticketId);

    /** Belirtilen kullanıcının yüklediği dosya kayıtlarını getirir. */
    List<Attachment> getAttachmentsByUploadedBy(Long userId);
}

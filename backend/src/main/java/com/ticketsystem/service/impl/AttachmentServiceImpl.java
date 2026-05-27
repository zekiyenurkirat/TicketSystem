package com.ticketsystem.service.impl;

import com.ticketsystem.entity.Attachment;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.User;
import com.ticketsystem.repository.AttachmentRepository;
import com.ticketsystem.service.AttachmentService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** {@link AttachmentService} arayüzünün varsayılan uygulaması. */
@Service
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TicketService ticketService;
    private final UserService userService;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository,
                                 TicketService ticketService,
                                 UserService userService) {
        this.attachmentRepository = attachmentRepository;
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Attachment saveAttachmentRecord(Long ticketId, Long uploadedById,
                                           String fileName, String fileType,
                                           String filePath, Long fileSize) {
        Ticket ticket = ticketService.getTicketById(ticketId);
        User uploadedBy = userService.getUserById(uploadedById);

        if (!uploadedBy.isActive()) {
            throw new RuntimeException("Pasif kullanıcı dosya ekleyemez. id: " + uploadedById);
        }

        if (fileName == null || fileName.isBlank()) {
            throw new RuntimeException("Dosya adı boş olamaz.");
        }

        if (fileType == null || fileType.isBlank()) {
            throw new RuntimeException("Dosya tipi boş olamaz.");
        }

        if (filePath == null || filePath.isBlank()) {
            throw new RuntimeException("Dosya yolu boş olamaz.");
        }

        if (fileSize == null) {
            throw new RuntimeException("Dosya boyutu boş olamaz.");
        }

        if (fileSize <= 0) {
            throw new RuntimeException("Dosya boyutu sıfırdan büyük olmalıdır.");
        }

        Attachment attachment = new Attachment();
        attachment.setTicket(ticket);
        attachment.setUploadedBy(uploadedBy);
        attachment.setFileName(fileName);
        attachment.setFileType(fileType);
        attachment.setFilePath(filePath);
        attachment.setFileSize(fileSize);

        return attachmentRepository.save(attachment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attachment> getAttachmentsByTicket(Long ticketId) {
        Ticket ticket = ticketService.getTicketById(ticketId);
        return attachmentRepository.findByTicket(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attachment> getAttachmentsByUploadedBy(Long userId) {
        User user = userService.getUserById(userId);
        return attachmentRepository.findByUploadedBy(user);
    }
}

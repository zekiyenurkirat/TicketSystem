package com.ticketsystem.service.impl;

import com.ticketsystem.core.exception.BusinessRuleException;
import com.ticketsystem.dto.request.SaveAttachmentRequest;
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
    public Attachment saveAttachmentRecord(SaveAttachmentRequest request) {
        Ticket ticket = ticketService.getTicketById(request.getTicketId());
        User uploadedBy = userService.getUserById(request.getUploadedById());

        if (!uploadedBy.isActive()) {
            throw new BusinessRuleException("Pasif kullanıcı dosya ekleyemez. id: " + request.getUploadedById());
        }

        if (request.getFileName() == null || request.getFileName().isBlank()) {
            throw new BusinessRuleException("Dosya adı boş olamaz.");
        }

        if (request.getFileType() == null || request.getFileType().isBlank()) {
            throw new BusinessRuleException("Dosya tipi boş olamaz.");
        }

        if (request.getFilePath() == null || request.getFilePath().isBlank()) {
            throw new BusinessRuleException("Dosya yolu boş olamaz.");
        }

        if (request.getFileSize() == null) {
            throw new BusinessRuleException("Dosya boyutu boş olamaz.");
        }

        if (request.getFileSize() <= 0) {
            throw new BusinessRuleException("Dosya boyutu sıfırdan büyük olmalıdır.");
        }

        Attachment attachment = new Attachment();
        attachment.setTicket(ticket);
        attachment.setUploadedBy(uploadedBy);
        attachment.setFileName(request.getFileName());
        attachment.setFileType(request.getFileType());
        attachment.setFilePath(request.getFilePath());
        attachment.setFileSize(request.getFileSize());

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

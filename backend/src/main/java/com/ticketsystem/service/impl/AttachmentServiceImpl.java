package com.ticketsystem.service.impl;

import com.ticketsystem.core.exception.BusinessRuleException;
import com.ticketsystem.dto.request.SaveAttachmentRequest;
import com.ticketsystem.entity.Attachment;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.Role;
import com.ticketsystem.repository.AttachmentRepository;
import com.ticketsystem.service.AttachmentService;
import com.ticketsystem.service.FileStorageService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** {@link AttachmentService} arayüzünün varsayılan uygulaması. */
@Service
public class AttachmentServiceImpl implements AttachmentService {

    /** Maksimum izin verilen dosya boyutu: 10 MB. */
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;

    /**
     * İzin verilen dosya uzantıları ve bunların beklenen MIME type eşleşmeleri.
     * Whitelist tabanlı; bu listede olmayan uzantılar reddedilir.
     */
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "pdf",  "application/pdf",
            "doc",  "application/msword",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "png",  "image/png",
            "jpg",  "image/jpeg",
            "jpeg", "image/jpeg",
            "txt",  "text/plain"
    );

    private final AttachmentRepository attachmentRepository;
    private final TicketService ticketService;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository,
                                 TicketService ticketService,
                                 UserService userService,
                                 FileStorageService fileStorageService) {
        this.attachmentRepository = attachmentRepository;
        this.ticketService = ticketService;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
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
    @Transactional
    public Attachment uploadAttachment(Long ticketId, MultipartFile file) {
        // 1. Ticket bulunur; CUSTOMER ownership zinciri burada devreye girer.
        Ticket ticket = ticketService.getTicketById(ticketId);

        // 2. Yükleyen kullanıcı token'dan belirlenir; client'tan alınmaz.
        User currentUser = getCurrentUser();

        // 3. Pasif kullanıcı upload yapamaz.
        if (!currentUser.isActive()) {
            throw new BusinessRuleException("Pasif kullanıcı dosya ekleyemez.");
        }

        // 4. Dosya boyutu ve boşluk kontrolü.
        validateFile(file);

        // 5. Orijinal dosya adı alınır.
        String originalFilename = file.getOriginalFilename();

        // 6. Dosya adı güvenlik kontrolü (null, blank, path traversal).
        validateOriginalFilename(originalFilename);

        // 7. Uzantı çıkarılır ve whitelist kontrolü yapılır.
        String extension = extractExtension(originalFilename);

        // 8. Uzantı ve MIME type uyumu kontrol edilir.
        validateFileType(extension, file.getContentType());

        // 9. Tüm validasyonlar geçtikten sonra dosya diske yazılır.
        String storedFileName = fileStorageService.storeFile(file, extension);

        // 10. DB metadata kaydı oluşturulur.
        Attachment attachment = new Attachment();
        attachment.setTicket(ticket);
        attachment.setUploadedBy(currentUser);
        attachment.setFileName(originalFilename);          // orijinal ad
        attachment.setFileType(file.getContentType());     // doğrulanmış MIME
        attachment.setFileSize(file.getSize());            // server-side boyut
        attachment.setFilePath(storedFileName);            // uuid.ext — client'a dönmez

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
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.CUSTOMER
                && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("Yalnızca kendi yüklediğiniz dosyaları listeleyebilirsiniz.");
        }
        User user = userService.getUserById(userId);
        return attachmentRepository.findByUploadedBy(user);
    }

    // -------------------------------------------------------------------------
    // Dosya doğrulama helper metotları
    // -------------------------------------------------------------------------

    /**
     * Dosya null, boş veya izin verilen maksimum boyutu aşıyor mu kontrol eder.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Yüklenecek dosya boş olamaz.");
        }
        if (file.getSize() == 0) {
            throw new BusinessRuleException("Yüklenecek dosya boş olamaz.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessRuleException("Dosya boyutu 10 MB sınırını aşamaz.");
        }
    }

    /**
     * Orijinal dosya adını null, blank ve path traversal riski açısından kontrol eder.
     */
    private void validateOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessRuleException("Dosya adı boş olamaz.");
        }
        if (originalFilename.contains("..") ||
            originalFilename.contains("/")  ||
            originalFilename.contains("\\")) {
            throw new BusinessRuleException("Güvensiz dosya adı.");
        }
    }

    /**
     * Orijinal dosya adından uzantıyı çıkarır ve küçük harfe çevirir.
     *
     * @param originalFilename doğrulanmış, null olmayan dosya adı
     * @return noktasız, küçük harfli uzantı (örn: "pdf", "png")
     * @throws BusinessRuleException uzantı bulunamadığında
     */
    private String extractExtension(String originalFilename) {
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            throw new BusinessRuleException("Dosya uzantısı bulunamadı.");
        }
        return originalFilename.substring(dotIndex + 1).toLowerCase();
    }

    /**
     * Uzantının whitelist'te olduğunu ve MIME type ile uyumlu olduğunu doğrular.
     *
     * @param extension   küçük harfli, noktasız uzantı
     * @param contentType dosyanın MIME type bilgisi
     * @throws BusinessRuleException izin verilmeyen uzantı veya MIME uyumsuzluğu
     */
    private void validateFileType(String extension, String contentType) {
        if (!ALLOWED_TYPES.containsKey(extension)) {
            throw new BusinessRuleException("Bu dosya türüne izin verilmiyor.");
        }
        if (contentType == null) {
            throw new BusinessRuleException("Dosya türü ve uzantısı uyuşmuyor.");
        }
        String expectedMime = ALLOWED_TYPES.get(extension);
        if (!expectedMime.equals(contentType)) {
            throw new BusinessRuleException("Dosya türü ve uzantısı uyuşmuyor.");
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }
}

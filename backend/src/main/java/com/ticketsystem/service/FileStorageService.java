package com.ticketsystem.service;

import com.ticketsystem.core.exception.BusinessRuleException;
import com.ticketsystem.core.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Fiziksel dosya depolama ve erişim işlemlerini yönetir.
 *
 * <p>Sorumluluklar:</p>
 * <ul>
 *   <li>Upload dizinini başlangıçta oluşturma</li>
 *   <li>MultipartFile'ı UUID tabanlı adla diske yazma</li>
 *   <li>Kaydedilmiş dosyayı Resource olarak okuma</li>
 * </ul>
 *
 * <p>Dosya boyutu, uzantı ve MIME type doğrulamaları bu sınıfın sorumluluğu değildir;
 * bu kontroller {@link com.ticketsystem.service.impl.AttachmentServiceImpl} içinde yapılır.</p>
 */
@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    /** Upload dizininin mutlak ve normalize edilmiş yolu. */
    private Path storageRoot;

    /**
     * Uygulama başlangıcında upload dizinini hazırlar.
     * Dizin yoksa oluşturur; varsa sessizce geçer.
     */
    @PostConstruct
    public void init() {
        storageRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new RuntimeException("Upload klasörü oluşturulamadı: " + storageRoot, e);
        }
    }

    /**
     * Verilen dosyayı UUID tabanlı bir adla storage dizinine yazar.
     *
     * <p>Dosya adı olarak {@code UUID.randomUUID() + "." + extension} kullanılır.
     * Orijinal dosya adı bu metot tarafından kullanılmaz; güvenli üretilmiş ad
     * diskte saklanır, orijinal ad ise DB'de {@code fileName} sütununa ayrıca kaydedilir.</p>
     *
     * @param file      yüklenecek dosya
     * @param extension doğrulanmış uzantı (noktasız, örn: "pdf", "png")
     * @return DB'ye kaydedilecek dosya adı (örn: "a1b2c3d4-....pdf")
     * @throws BusinessRuleException storage sınırı ihlali veya yazma hatası
     */
    public String storeFile(MultipartFile file, String extension) {
        String generatedFileName = UUID.randomUUID() + "." + extension;
        Path targetPath = storageRoot.resolve(generatedFileName).normalize();

        if (!targetPath.startsWith(storageRoot)) {
            throw new BusinessRuleException("Güvensiz dosya yolu tespit edildi.");
        }

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessRuleException("Dosya diske yazılamadı: " + generatedFileName);
        }

        return generatedFileName;
    }

    /**
     * DB'de kayıtlı dosya adından fiziksel dosyayı bulup {@link Resource} olarak döner.
     *
     * @param storedFileName DB'deki {@code file_path} değeri (örn: "a1b2c3d4-....pdf")
     * @return okunabilir {@link Resource}
     * @throws BusinessRuleException  storage sınırı dışına çıkma girişimi
     * @throws ResourceNotFoundException dosya diskte bulunamadı veya okunamıyor
     */
    public Resource loadFileAsResource(String storedFileName) {
        Path filePath = storageRoot.resolve(storedFileName).normalize();

        if (!filePath.startsWith(storageRoot)) {
            throw new BusinessRuleException("Güvensiz dosya yolu tespit edildi.");
        }

        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Dosya bulunamadı: " + storedFileName);
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.isReadable()) {
                throw new ResourceNotFoundException("Dosya okunamıyor: " + storedFileName);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Dosya yolu hatalı: " + storedFileName);
        }
    }
}

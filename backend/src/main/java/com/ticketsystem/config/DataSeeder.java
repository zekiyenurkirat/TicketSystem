package com.ticketsystem.config;

import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.Role;
import com.ticketsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Uygulama başlangıcında boş veritabanını demo hesabıyla hazırlar.
 *
 * <p>Çalışma koşulu: Sistemde hiç MANAGER yoksa ve
 * manager2026@example.com adresi kayıtlı değilse
 * bir başlangıç MANAGER hesabı oluşturulur.</p>
 *
 * <p>Sistemde zaten MANAGER varsa veya ilgili e-posta mevcutsa
 * seed tamamen atlanır — duplicate kullanıcı oluşturulmaz.</p>
 */
@Slf4j
@Component
public class DataSeeder implements ApplicationRunner {

    private static final String SEED_EMAIL    = "manager2026@example.com";
    private static final String SEED_PASSWORD = "Sifre1234";

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // 1. Katman: Sistemde herhangi bir MANAGER zaten varsa atla
        if (userRepository.existsByRole(Role.MANAGER)) {
            log.info("Seed atlandı: sistemde zaten MANAGER mevcut.");
            return;
        }

        // 2. Katman: Seed e-postası başka bir rolle kayıtlıysa atla
        if (userRepository.existsByEmail(SEED_EMAIL)) {
            log.info("Seed atlandı: {} zaten kayıtlı.", SEED_EMAIL);
            return;
        }

        // Her iki kontrol de geçildi — başlangıç MANAGER hesabı oluştur
        User manager = new User();
        manager.setFirstName("Demo");
        manager.setLastName("Manager");
        manager.setEmail(SEED_EMAIL);
        manager.setPasswordHash(passwordEncoder.encode(SEED_PASSWORD));
        manager.setRole(Role.MANAGER);
        manager.setActive(true);

        userRepository.save(manager);

        log.info("Seed: başlangıç MANAGER hesabı oluşturuldu → {}", SEED_EMAIL);
    }
}

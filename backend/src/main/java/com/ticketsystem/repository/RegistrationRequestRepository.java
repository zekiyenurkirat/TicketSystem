package com.ticketsystem.repository;

import com.ticketsystem.entity.RegistrationRequest;
import com.ticketsystem.entity.enums.RegistrationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Kayıt talebi veritabanı erişim arayüzü. */
public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {

    /** Belirtilen statüdeki tüm kayıt taleplerini getirir. */
    List<RegistrationRequest> findByStatus(RegistrationRequestStatus status);

    /** Verilen email için belirtilen statülerden herhangi birinde kayıt talebi var mı kontrol eder. */
    boolean existsByEmailAndStatusIn(String email, List<RegistrationRequestStatus> statuses);
}

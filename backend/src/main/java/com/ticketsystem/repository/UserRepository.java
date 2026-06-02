package com.ticketsystem.repository;

import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Kullanıcı veritabanı erişim arayüzü. */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Email adresine göre kullanıcı arar. */
    Optional<User> findByEmail(String email);

    /** Verilen email adresine sahip bir kullanıcı var mı kontrol eder. */
    boolean existsByEmail(String email);

    /** Belirtilen role sahip tüm kullanıcıları getirir. */
    List<User> findByRole(Role role);

    /** Belirtilen role sahip ve aktif olan kullanıcıları getirir. */
    List<User> findByRoleAndActiveTrue(Role role);

    /** Belirtilen role sahip en az bir kullanıcı var mı kontrol eder. */
    boolean existsByRole(Role role);
}

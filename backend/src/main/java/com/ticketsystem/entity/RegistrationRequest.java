package com.ticketsystem.entity;

import com.ticketsystem.entity.enums.RegistrationRequestStatus;
import com.ticketsystem.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * CUSTOMER'ların sisteme kayıt başvurularını temsil eden entity.
 * "registration_requests" tablosuna karşılık gelir.
 * Manager onaylanmadan kullanıcı hesabı oluşturulmaz.
 */
@Entity
@Table(name = "registration_requests")
@Getter
@Setter
@NoArgsConstructor
public class RegistrationRequest extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    /** BCrypt ile hashlenmiş şifre. Onay gerçekleştiğinde User oluştururken kullanılır. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** Kayıt talebinin hedef rolü. Servis tarafından sabit Role.CUSTOMER olarak atanır. */
    @Enumerated(EnumType.STRING)
    @Column(name = "requested_role", nullable = false)
    private Role requestedRole = Role.CUSTOMER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RegistrationRequestStatus status = RegistrationRequestStatus.PENDING;

    /** Manager'ın eklediği onay/red açıklaması. */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /** Talebi onaylayan veya reddeden manager. İşlem yapılmamışsa null. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id", nullable = true)
    private User reviewedBy;

    /** Onay veya reddin gerçekleştiği zaman. İşlem yapılmamışsa null. */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}

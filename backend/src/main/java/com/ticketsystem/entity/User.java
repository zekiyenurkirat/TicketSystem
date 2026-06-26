package com.ticketsystem.entity;

import com.ticketsystem.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Sisteme kayıtlı kullanıcıları temsil eden entity.
 * "users" tablosuna karşılık gelir.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    /** Kullanıcının giriş yapacağı e-posta adresi. */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /** BCrypt ile hashlenmiş şifre. Düz metin saklanmaz. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** Kullanıcının sistem rolü. */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    /**
     * Hesabın aktif olup olmadığını belirtir.
     * Kullanıcı silmek yerine pasif yapmak (soft delete) tercih edilir.
     */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /** Google Authenticator için Base32 kodlanmış TOTP secret. Null ise 2FA kurulmamış. */
    @Column(name = "totp_secret")
    private String totpSecret;

    /** true ise login sırasında TOTP kodu istenir. Varsayılan false — mevcut kullanıcılar etkilenmez. */
    @Column(name = "totp_enabled", nullable = false)
    private boolean totpEnabled = false;

    /** Bu kullanıcının oluşturduğu ticket'lar. */
    @OneToMany(mappedBy = "createdBy")
    private List<Ticket> createdTickets = new ArrayList<>();

    /** Bu kullanıcıya (agent) atanmış ticket'lar. */
    @OneToMany(mappedBy = "assignedTo")
    private List<Ticket> assignedTickets = new ArrayList<>();
}

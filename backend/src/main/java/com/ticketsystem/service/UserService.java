package com.ticketsystem.service;

import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.Role;

import java.util.List;

/** Kullanıcı yönetimi iş kurallarını tanımlar. */
public interface UserService {

    /** Yeni kullanıcı oluşturur ve kaydeder. */
    User createUser(String firstName, String lastName, String email, String passwordHash, Role role);

    /** ID ile kullanıcı getirir. */
    User getUserById(Long id);

    /** Email ile kullanıcı getirir. */
    User getUserByEmail(String email);

    /** Belirtilen role sahip tüm kullanıcıları getirir. */
    List<User> getUsersByRole(Role role);

    /** Belirtilen role sahip ve aktif olan kullanıcıları getirir. */
    List<User> getActiveUsersByRole(Role role);

    /** Kullanıcıyı pasife alır. */
    User deactivateUser(Long id);
}

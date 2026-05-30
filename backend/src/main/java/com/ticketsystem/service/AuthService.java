package com.ticketsystem.service;

import com.ticketsystem.dto.request.LoginRequest;
import com.ticketsystem.dto.response.AuthResponse;

/** Kimlik doğrulama iş kurallarını tanımlar. */
public interface AuthService {

    /** Kullanıcı girişi yapar ve JWT token döner. */
    AuthResponse login(LoginRequest request);
}

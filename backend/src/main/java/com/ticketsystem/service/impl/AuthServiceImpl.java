package com.ticketsystem.service.impl;

import com.ticketsystem.dto.request.LoginRequest;
import com.ticketsystem.dto.response.AuthResponse;
import com.ticketsystem.entity.User;
import com.ticketsystem.security.JwtService;
import com.ticketsystem.service.AuthService;
import com.ticketsystem.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/** {@link AuthService} arayüzünün varsayılan uygulaması. */
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserService userService,
                           JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userService.getUserByEmail(request.getEmail());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());

        String token = jwtService.generateToken(extraClaims, userDetails);

        return new AuthResponse(token, user.getEmail(), user.getRole(), jwtExpiration);
    }
}

package com.ticketsystem.service.impl;

import com.ticketsystem.core.exception.BusinessRuleException;
import com.ticketsystem.dto.request.EnableTotpRequest;
import com.ticketsystem.dto.request.LoginRequest;
import com.ticketsystem.dto.request.VerifyTotpRequest;
import com.ticketsystem.dto.response.AuthResponse;
import com.ticketsystem.dto.response.TotpSetupResponse;
import com.ticketsystem.entity.User;
import com.ticketsystem.repository.UserRepository;
import com.ticketsystem.security.JwtService;
import com.ticketsystem.service.AuthService;
import com.ticketsystem.service.TotpService;
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

    private static final String ISSUER = "TicketSystem";

    private final AuthenticationManager authenticationManager;
    private final UserService           userService;
    private final UserRepository        userRepository;
    private final JwtService            jwtService;
    private final TotpService           totpService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserService userService,
                           UserRepository userRepository,
                           JwtService jwtService,
                           TotpService totpService) {
        this.authenticationManager = authenticationManager;
        this.userService    = userService;
        this.userRepository = userRepository;
        this.jwtService     = jwtService;
        this.totpService    = totpService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.getUserByEmail(request.getEmail());

        if (user.isTotpEnabled()) {
            // Parola doğru ama 2FA gerekiyor — challenge token döner, tam JWT verilmez
            String challengeToken = jwtService.generateChallengeToken(user.getEmail());
            return AuthResponse.challenge(user.getEmail(), challengeToken);
        }

        return buildFullAuth(user, userDetails);
    }

    @Override
    public TotpSetupResponse setupTotp(String email) {
        User user = userService.getUserByEmail(email);
        String secret = totpService.generateSecret();
        user.setTotpSecret(secret);
        userRepository.save(user);
        String uri = totpService.buildOtpAuthUri(secret, email, ISSUER);
        return new TotpSetupResponse(secret, uri);
    }

    @Override
    public void enableTotp(String email, EnableTotpRequest request) {
        User user = userService.getUserByEmail(email);
        if (user.getTotpSecret() == null) {
            throw new BusinessRuleException("Önce 2FA kurulumu başlatılmalıdır (/2fa/setup).");
        }
        if (!totpService.validateCode(user.getTotpSecret(), request.getCode())) {
            throw new BusinessRuleException("Geçersiz TOTP kodu. Lütfen Google Authenticator'dan güncel kodu girin.");
        }
        user.setTotpEnabled(true);
        userRepository.save(user);
    }

    @Override
    public AuthResponse verifyTotp(VerifyTotpRequest request) {
        String email;
        try {
            email = jwtService.extractEmailFromChallengeToken(request.getChallengeToken());
        } catch (Exception e) {
            throw new BusinessRuleException("Challenge token geçersiz veya süresi dolmuş. Lütfen tekrar giriş yapın.");
        }

        User user = userService.getUserByEmail(email);

        if (!user.isTotpEnabled() || user.getTotpSecret() == null) {
            throw new BusinessRuleException("Bu kullanıcı için 2FA etkin değil.");
        }
        if (!totpService.validateCode(user.getTotpSecret(), request.getCode())) {
            throw new BusinessRuleException("Geçersiz veya süresi dolmuş TOTP kodu.");
        }

        // Challenge token geçerli + TOTP doğru → tam JWT üret
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
        return buildFullAuth(user, userDetails);
    }

    // ── yardımcı ────────────────────────────────────────────────────────────

    private AuthResponse buildFullAuth(User user, UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());
        String token = jwtService.generateToken(extraClaims, userDetails);
        return AuthResponse.fullAuth(token, user.getEmail(), user.getRole(), jwtExpiration);
    }
}

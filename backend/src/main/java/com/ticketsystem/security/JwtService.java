package com.ticketsystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** JWT token üretme, doğrulama ve claim okuma işlemlerini yönetir. */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    /** Ek claim içermeyen token üretir. */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /** Verilen ek claim'lerle birlikte token üretir. */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /** Token'dan kullanıcı adını (email) çeker. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Token'dan belirtilen claim'i çeker. */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    /** 2FA için kısa ömürlü (5 dk) challenge token üretir; tam yetki içermez. */
    public String generateChallengeToken(String email) {
        return Jwts.builder()
                .claims(Map.of("purpose", "2fa-challenge"))
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 300_000L))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Challenge token'ın imzasını ve amacını doğrular; geçerliyse email döner.
     * Süresi dolmuş veya yanlış amaçlı token'da IllegalArgumentException fırlatır.
     */
    public String extractEmailFromChallengeToken(String token) {
        Claims claims = extractAllClaims(token);
        if (!"2fa-challenge".equals(claims.get("purpose", String.class))) {
            throw new IllegalArgumentException("Geçersiz challenge token.");
        }
        return claims.getSubject();
    }

    /** Token'ın verilen kullanıcıya ait ve geçerli olup olmadığını doğrular.
     *  Süresi dolmuş, bozuk veya imzası geçersiz token için false döner. */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

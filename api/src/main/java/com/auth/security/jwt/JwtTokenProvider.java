package com.auth.security.jwt;

import com.auth.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Primary
@Component
public class JwtTokenProvider implements TokenProvider {


    private final SecretKey key = Keys.hmacShaKeyFor(
            "my-secret-key-my-secret-key-my-secret-key-123456".getBytes()
    );

    private final long accessTokenValidity = 1000 * 60 * 15;

    // ================= GENERATE =================
    @Override
    public String generateToken(User user) {

        return Jwts.builder()
                .subject(user.getEmail()) // FIX mới (không dùng setSubject)
                .claim("roles", List.of("USER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(key)
                .compact();
    }

    // ================= VALIDATE =================
    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ================= EMAIL =================
    @Override
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // ================= ROLES =================
    @Override
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object roles = claims.get("roles");

        if (roles instanceof List<?>) {
            return ((List<?>) roles)
                    .stream()
                    .map(String::valueOf)
                    .toList();
        }

        return List.of();
    }
}
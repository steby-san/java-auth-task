package com.auth.service;

import com.auth.model.RefreshToken;
import com.auth.model.User;
import com.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpiration;

    public String createRefreshToken(User user, String ip, String userAgent) {

        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .tokenValue(token)
                .clientIp(ip)
                .userAgentString(userAgent)
                .isRevoked(false)
                .expiresAt(
                        Instant.now().plusSeconds(604800)
                )
                .build();

        refreshTokenRepository.save(refreshToken);

        return token;


    }
}
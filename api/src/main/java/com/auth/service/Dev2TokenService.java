package com.auth.service;

import com.auth.model.RefreshToken;
import com.auth.model.User;
import com.auth.repository.RefreshTokenRepository;
import com.auth.security.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class Dev2TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    @Value("${app.refresh-token.expiration-seconds:604800}")
    private long refreshTokenExpirationSeconds;

    // ================= CREATE =================
    @Transactional
    public String generateAndStoreRefreshToken(User user) {

        // revoke all old tokens (ROTATION BASE RULE)
        refreshTokenRepository.revokeAllUserTokens(UUID.fromString(user.getId()));

        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .tokenValue(tokenValue)
                .expiresAt(Instant.now().plusSeconds(refreshTokenExpirationSeconds))
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }

    // overload with request info
    @Transactional
    public String generateAndStoreRefreshToken(User user, HttpServletRequest request) {

        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        refreshTokenRepository.revokeAllUserTokens(UUID.fromString(user.getId()));

        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .tokenValue(tokenValue)
                .expiresAt(Instant.now().plusSeconds(refreshTokenExpirationSeconds))
                .isRevoked(false)
                .clientIp(clientIp)
                .userAgentString(userAgent)
                .build();

        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }

    // ================= VERIFY =================
    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenValue(token)
                .orElse(null);

        if (refreshToken == null) {
            return null;
        }

        if (refreshToken.getIsRevoked() || refreshToken.isExpired()) {
            return refreshToken;
        }

        return refreshToken;
    }

    // ================= ROTATION CORE (IMPORTANT) =================
    @Transactional
    public String rotateToken(String refreshTokenValue,
                              HttpServletRequest request, HttpServletResponse response) {

        RefreshToken oldToken = refreshTokenRepository
                .findByTokenValue(refreshTokenValue)
                .orElse(null);

        // ❌ INVALID TOKEN CASE
        if (oldToken == null || oldToken.getIsRevoked() || oldToken.isExpired()) {

            if (oldToken != null) {
                // 🔥 FORCE LOGOUT ALL DEVICES
                refreshTokenRepository.revokeAllUserTokens(UUID.fromString(oldToken.getUser().getId()));
            }

            throw new RuntimeException("401 Unauthorized - Invalid Refresh Token");
        }

        User user = oldToken.getUser();

        // ❌ revoke old token
        oldToken.setIsRevoked(true);
        oldToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(oldToken);

        // ✔ generate NEW access token
        String newAccessToken = tokenProvider.generateToken(user);

        // ✔ generate NEW refresh token (ROTATION)
        String newRefreshToken = generateAndStoreRefreshToken(user, request);

        return newAccessToken; // return access token only
    }

    // ================= REVOKE =================
    @Transactional
    public void revokeToken(String tokenValue) {

        refreshTokenRepository.findByTokenValue(tokenValue)
                .ifPresent(token -> {
                    token.setIsRevoked(true);
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void revokeAllUserTokens(String userId) {
        refreshTokenRepository.revokeAllUserTokens(UUID.fromString(userId));
    }

    // ================= CLEAN EXPIRED =================
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteAllExpired();
    }

    // ================= IP HELPER =================
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-FORWARDED-FOR");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
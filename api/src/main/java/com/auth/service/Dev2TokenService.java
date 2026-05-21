package com.auth.service;

import com.auth.model.RefreshToken;
import com.auth.model.User;
import com.auth.repository.RefreshTokenRepository;
import com.auth.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Value("${app.refresh-token.expiration-seconds:604800}")
    private long refreshTokenExpirationSeconds; // Default 7 days

    /**
     * Generate refresh token mới và lưu vào database
     */
    @Transactional
    public String generateAndStoreRefreshToken(User user) {
        return generateAndStoreRefreshToken(user, null, null);
    }

    /**
     * Generate refresh token mới với thông tin client
     */
    @Transactional
    public String generateAndStoreRefreshToken(User user, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        return generateAndStoreRefreshToken(user, clientIp, userAgent);
    }

    /**
     * Generate refresh token mới
     */
    @Transactional
    public String generateAndStoreRefreshToken(User user, String clientIp, String userAgent) {
        // Revoke tất cả refresh tokens cũ của user này
        refreshTokenRepository.revokeAllUserTokens(UUID.fromString(user.getId()));

        // Tạo refresh token mới
        String tokenValue = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(refreshTokenExpirationSeconds);

        RefreshToken refreshToken = RefreshToken.builder()
                .id(String.valueOf(UUID.randomUUID()))
                .user(user)
                .tokenValue(tokenValue)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .clientIp(clientIp)
                .userAgentString(userAgent)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Generated refresh token for user: {} (expires: {})", user.getEmail(), expiresAt);

        return tokenValue;
    }

    /**
     * Verify refresh token
     */
    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token) {
        return refreshTokenRepository.findByTokenValue(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
    }

    /**
     * Kiểm tra refresh token có hợp lệ không
     */
    public boolean isTokenValid(RefreshToken token) {
        return token != null && !token.getIsRevoked() && !token.isExpired();
    }

    /**
     * Revoke refresh token
     */
    @Transactional
    public void revokeToken(String tokenValue) {
        refreshTokenRepository.findByTokenValue(tokenValue)
                .ifPresent(token -> {
                    token.setIsRevoked(true);
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                    log.info("Revoked refresh token: {}", tokenValue);
                });
    }

    /**
     * Revoke tất cả tokens của user
     */
    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
        log.info("Revoked all tokens for user: {}", userId);
    }

    /**
     * Xóa các token đã hết hạn
     */
    @Transactional
    public void deleteExpiredTokens() {
        int deletedCount = refreshTokenRepository.deleteAllExpired();
        log.info("Deleted {} expired refresh tokens", deletedCount);
    }

    /**
     * Refresh access token sử dụng refresh token
     */
    @Transactional
    public String refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = verifyRefreshToken(refreshTokenValue);

        if (!isTokenValid(refreshToken)) {
            throw new RuntimeException("Refresh token is invalid or expired");
        }

        // Revoke token cũ
        refreshToken.setIsRevoked(true);
        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        // Generate token mới
        User user = refreshToken.getUser();
        return generateAndStoreRefreshToken(user, refreshToken.getClientIp(), refreshToken.getUserAgentString());
    }

    /**
     * Lấy IP address từ request
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-FORWARDED-FOR");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
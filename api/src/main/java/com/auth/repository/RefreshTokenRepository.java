package com.auth.repository;

import com.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByTokenValue(String tokenValue);

    List<RefreshToken> findByUserId(String userId);

    List<RefreshToken> findByIsRevokedTrue();

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :now")
    List<RefreshToken> findAllExpired(Instant now);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true, rt.revokedAt = :now WHERE rt.user.id = :userId AND rt.isRevoked = false")
    int revokeAllUserTokens(String userId, Instant now);

    default int revokeAllUserTokens(String userId) {
        return revokeAllUserTokens(userId, Instant.now());
    }

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteAllExpired(Instant now);

    default int deleteAllExpired() {
        return deleteAllExpired(Instant.now());
    }
}
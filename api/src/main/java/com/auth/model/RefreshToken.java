package com.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_rt_user", columnList = "user_id"),
                @Index(name = "idx_rt_expiry", columnList = "expires_at"),
                @Index(name = "idx_rt_revoked", columnList = "is_revoked")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(
            name = "token_value",
            nullable = false,
            unique = true,
            length = 255
    )
    private String tokenValue;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Builder.Default
    @Column(
            name = "is_revoked",
            nullable = false,
            columnDefinition = "TINYINT(1) DEFAULT 0"
    )
    private Boolean isRevoked = false;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "user_agent_string", length = 500)
    private String userAgentString;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return !Boolean.TRUE.equals(isRevoked)
                && !isExpired();
    }
}
package com.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "token_value")
    private String tokenValue;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_revoked")
    private boolean revoked;

    @Column(name = "client_ip")
    private String clientIp;

    @Column(name = "user_agent_string")
    private String userAgent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
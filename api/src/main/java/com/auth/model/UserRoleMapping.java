package com.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_role_mappings",
        indexes = {
                @Index(name = "idx_urm_user", columnList = "user_id"),
                @Index(name = "idx_urm_role", columnList = "role_id")
        })
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserRoleMapping {

    @EmbeddedId
    private UserRoleMappingId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "assigned_at", updatable = false)
    private Instant assignedAt;

    @Column(name = "assigned_by", columnDefinition = "CHAR(36)")
    private UUID assignedBy;
}

// 👇 Embedded ID cho composite primary key
@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode  // ✅ Tự động generate equals() và hashCode()
class UserRoleMappingId implements java.io.Serializable {

    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private UUID userId;

    @Column(name = "role_id", columnDefinition = "CHAR(36)")
    private UUID roleId;
}
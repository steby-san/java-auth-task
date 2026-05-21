package com.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "role_permission_mappings",
        indexes = {
                @Index(name = "idx_rpm_role", columnList = "role_id"),
                @Index(name = "idx_rpm_perm", columnList = "permission_id")
        })
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RolePermissionMapping {

    @EmbeddedId
    private RolePermissionMappingId id;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @MapsId("permissionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @CreationTimestamp
    @Column(name = "assigned_at", updatable = false)
    private Instant assignedAt;
}

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
class RolePermissionMappingId implements java.io.Serializable {
    @Column(name = "role_id", columnDefinition = "CHAR(36)")
    private String roleId;

    @Column(name = "permission_id", columnDefinition = "CHAR(36)")
    private String permissionId;
}
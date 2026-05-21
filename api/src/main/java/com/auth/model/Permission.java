package com.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "permissions", indexes = @Index(name = "idx_permissions_code", columnList = "permission_code"))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Permission {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "permission_code", nullable = false, unique = true, length = 50)
    private String permissionCode;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(name = "action_type", length = 20)
    private String actionType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();
}
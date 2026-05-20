package com.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @Column(length = 36)
    private String id;

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    @Column(name = "permission_code", nullable = false, unique = true)
    private String permissionCode;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "action_type")
    private String actionType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
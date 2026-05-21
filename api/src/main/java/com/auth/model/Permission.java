package com.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    @Column(name = "permission_code", nullable = false, unique = true)
    private String permissionCode;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "action_type")
    private String actionType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        createdAt = LocalDateTime.now();
    }
}
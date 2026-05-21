package com.auth.model;

import com.auth.model.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_provider_lookup", columnList = "auth_provider, external_provider_id")
})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Builder.Default  // ✅ Thêm annotation này
    @Column(name = "is_enabled", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isEnabled = true;

    @Builder.Default  // ✅ Thêm annotation này
    @Column(name = "is_account_non_locked", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isAccountNonLocked = true;

    @Builder.Default  // ✅ Thêm annotation này
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "external_provider_id", length = 255)
    private String externalProviderId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Builder.Default  // ✅ Thêm annotation này
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role_mappings",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Helper method
    public Set<String> getRoleCodes() {
        return roles.stream()
                .map(Role::getRoleCode)
                .collect(java.util.stream.Collectors.toSet());
    }
}
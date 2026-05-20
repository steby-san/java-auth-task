package com.auth.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")  // ✅ Khớp với tên bảng trong DB
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String firstName;
    private String lastName;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Boolean accountNonLocked = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")  // ✅ Khớp với column trong DB
    private AuthProvider provider;  // ✅ Đã import AuthProvider

    @Column(name = "provider_id")  // ✅ Khớp với column
    private String providerId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",  // ✅ Khớp với tên bảng join
            joinColumns = @JoinColumn(name = "user_id"),  // ✅ Khớp với column
            inverseJoinColumns = @JoinColumn(name = "role_id")  // ✅ Khớp với column
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();  // ✅ Type là Set<Role>

    // === UserDetails Methods ===
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))  // ✅ role.getName() exists
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked != null && accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    // === Helper Methods ===
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return email;
    }
}
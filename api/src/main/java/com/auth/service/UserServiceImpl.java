package com.auth.service;

import com.auth.model.Role;
import com.auth.model.User;
import com.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public User register(User user) {

        // 1. check email tồn tại
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // 2. encode password (FIELD ĐÚNG LÀ password)
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        // 3. set id nếu chưa có
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }

        // 4. default role nếu chưa có
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Role role = new Role();
            role.setRoleCode("USER"); // hoặc "ROLE_USER" tùy hệ thống bạn

            user.setRoles(Set.of(role));
        }

        return userRepository.save(user);
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getRoleCode()))
                        .collect(Collectors.toList())
        );
    }


    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User findById(String id) {
        return userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
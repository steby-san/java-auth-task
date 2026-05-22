package com.auth.service;



import com.auth.dto.request.LoginRequest;
import com.auth.dto.request.RegisterRequest;
import com.auth.dto.response.AuthResponse;
import com.auth.model.RefreshToken;
import com.auth.model.User;
import com.auth.repository.UserRepository;
import com.auth.security.jwt.TokenProvider;
import com.auth.service.AuthService;
import com.auth.service.Dev2TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final Dev2TokenService dev2TokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse register(RegisterRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        userRepository.save(user);

        return new AuthResponse(tokenProvider.generateToken(user));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        // ⚠️ skip password check demo
        return new AuthResponse(tokenProvider.generateToken(user));
    }

    @Override
    public AuthResponse refresh(String refreshToken) {

        RefreshToken token = dev2TokenService.verifyRefreshToken(refreshToken);

        User user = token.getUser();
        return new AuthResponse(tokenProvider.generateToken(user));
    }
}
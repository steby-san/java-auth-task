package com.auth.controller;

import com.auth.dto.request.RegisterRequest;
import com.auth.model.User;
import com.auth.service.AuthService;
import com.auth.service.Dev2TokenService;
import com.auth.security.jwt.TokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final Dev2TokenService refreshTokenService;
    private final com.auth.service.UserService userService;

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request,
            HttpServletResponse response) {

        String email = request.get("email");
        String password = request.get("password");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        org.springframework.security.core.userdetails.User springUser = (org.springframework.security.core.userdetails.User) authentication
                .getPrincipal();

        User user = userService.findByEmail(springUser.getUsername());

        String accessToken = tokenProvider.generateToken(user);
        String refreshToken = refreshTokenService.generateAndStoreRefreshToken(user);

        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // dev
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "tokenType", "Bearer"));
    }

    // ================= REFRESH =================
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = "refresh_token") String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(
                refreshTokenService.rotateToken(refreshToken, request, response));
    }
}
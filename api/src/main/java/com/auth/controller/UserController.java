package com.auth.controller;

import com.auth.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    // ================= PROFILE USER =================
    @GetMapping("/users/profile")
    public ResponseEntity<?> profile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }

    // ================= ADMIN DASHBOARD =================
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminDashboard() {
        return ResponseEntity.ok(
                Map.of(
                        "message", "Welcome Admin",
                        "status", "OK"
                )
        );
    }
}
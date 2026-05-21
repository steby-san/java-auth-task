package com.auth.security.jwt;

import com.auth.model.User;
import java.util.List;

public interface TokenProvider {
    String generateToken(User user);
    boolean validateToken(String token);
    String getEmailFromToken(String token);
    List<String> getRolesFromToken(String token);
}
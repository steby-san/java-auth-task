package com.auth.service;

import com.auth.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User register(User user);

    User findByEmail(String email);

    User findById(String id);
}
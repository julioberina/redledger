package com.redledger.service;

import com.redledger.dto.LoginRequest;
import com.redledger.dto.LoginResponse;
import com.redledger.dto.RegisterRequest;
import com.redledger.entity.User;
import com.redledger.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(RegisterRequest request) {
        // TODO: Implement registration logic
        // 1. Check if username/email already exists
        // 2. Hash password
        // 3. Create user with ROLE_USER
        // 4. Create default checking account
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // TODO: Hash password!
        user.setEmail(request.getEmail());
        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        // TODO: Implement login logic
        // 1. Find user by username
        // 2. Verify password
        // 3. Generate JWT token
        // 4. Return token response
        throw new UnsupportedOperationException("Login not yet implemented");
    }
}

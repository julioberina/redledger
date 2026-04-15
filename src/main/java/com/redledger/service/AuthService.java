package com.redledger.service;

import com.redledger.dto.LoginRequest;
import com.redledger.dto.LoginResponse;
import com.redledger.dto.RegisterRequest;
import com.redledger.entity.User;
import com.redledger.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        // TODO (1.3): Add uniqueness check for username/email
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        // TODO (1.4): Find user, verify with passwordEncoder.matches(), generate JWT
        throw new UnsupportedOperationException("Login not yet implemented");
    }
}
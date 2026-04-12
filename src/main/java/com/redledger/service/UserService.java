package com.redledger.service;

import com.redledger.entity.User;
import com.redledger.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        // TODO: Implement user listing with proper authorization checks
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        // TODO: Implement authorization check - should users only see their own profile?
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(User user) {
        // TODO: Add password hashing, input validation, duplicate checks
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        // TODO: Add authorization check - admin only
        userRepository.deleteById(id);
    }
}

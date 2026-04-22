package com.redledger.service;

import com.redledger.entity.User;
import com.redledger.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}

	public List<User> getAllUsers() {
		// TODO: Implement user listing with proper authorization checks
		return userRepository.findByActiveTrue();
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

	public Optional<User> updateUserRole(Long id, String role) {
		return userRepository.findById(id).map(user -> {
			user.setRole(role);
			return userRepository.save(user);
		});
	}

	public void deleteUser(Long id) {
		userRepository.findById(id).ifPresent(user -> {
			user.setActive(false);
			userRepository.save(user);
		});
	}
}

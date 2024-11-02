package com.example.shopapp.service.impl;

import com.example.shopapp.dto.request.LoginRequest;
import com.example.shopapp.model.User;
import com.example.shopapp.model.UserRole;
import com.example.shopapp.repository.UserRepository;
import com.example.shopapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public User register(User request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(hashPassword(request.getPassword())); // Important: Hash the password
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(UserRole.USER);

        // Save user
        return userRepository.save(user);
    }

    // Example hashing method (you can use libraries like BCrypt or Argon2)
    private String hashPassword(String password) {
        // Implement your hashing logic here
        return password; // Replace this with hashed password
    }


    @Override
    @Transactional
    public User login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Check password (plain text comparison)
        if (!request.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Return user
        return user;
    }

    @Override
    @Transactional
    public User updateProfile(UUID userId, User request) {
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if new email is not taken by another user
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Update user details
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());

        // Save updated user
        return userRepository.save(user);
    }

    @Override
    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

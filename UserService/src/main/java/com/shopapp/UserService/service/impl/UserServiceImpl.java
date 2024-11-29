package com.shopapp.UserService.service.impl;

import com.shopapp.UserService.dto.user.request.LoginRequest;
import com.shopapp.UserService.dto.user.request.RegisterUserRequest;
import com.shopapp.UserService.dto.user.request.UpdateUserRequest;
import com.shopapp.UserService.dto.user.response.UserResponse;
import com.shopapp.UserService.exception.InvalidCredentialsException;
import com.shopapp.UserService.exception.ResourceNotFoundException;
import com.shopapp.UserService.exception.UserAlreadyExistsException;
import com.shopapp.UserService.exception.UserNotFoundException;
import com.shopapp.UserService.mapper.UserMapper;
import com.shopapp.UserService.model.User;
import com.shopapp.UserService.repository.UserRepository;
import com.shopapp.UserService.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            log.warn("Phone number already exists: {}", request.getPhoneNumber());
            throw new UserAlreadyExistsException("Phone number already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(request.getPassword()); // Password should be hashed in production

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        log.info("User login attempt with identifier: {}", request.getIdentifier());

        User user = userRepository.findByEmailOrPhoneNumber(request.getIdentifier(), request.getIdentifier())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid identifier or password"));

        if (!request.getPassword().equals(user.getPassword())) { // Use password hashing in production
            log.warn("Invalid credentials for identifier: {}", request.getIdentifier());
            throw new InvalidCredentialsException("Invalid identifier or password");
        }

        log.info("User logged in successfully with ID: {}", user.getId());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateUserRequest request) {
        log.info("Updating profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already exists");
        }

        if (!user.getPhoneNumber().equals(request.getPhoneNumber()) && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            log.warn("Phone number already exists: {}", request.getPhoneNumber());
            throw new UserAlreadyExistsException("Phone number already exists");
        }
        System.out.println("request consists of: "+request);
        userMapper.updateEntity(user, request);

        User updatedUser = userRepository.save(user);

        log.info("User profile updated successfully for ID: {}", updatedUser.getId());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findUserById(UUID userId) {
        log.info("Retrieving user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean doesUserExist(UUID userId) {
        log.info("Checking existence for user ID: {}", userId);
        return userRepository.existsById(userId);
    }

}

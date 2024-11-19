package com.example.shopapp.service.impl;

import com.example.shopapp.dto.request.LoginRequest;
import com.example.shopapp.dto.request.UserRequestDTO;
import com.example.shopapp.dto.response.UserResponse;
import com.example.shopapp.exception.InvalidCredentialsException;
import com.example.shopapp.exception.UserAlreadyExistsException;
import com.example.shopapp.exception.UserNotFoundException;
import com.example.shopapp.mapper.UserMapper;
import com.example.shopapp.model.User;
import com.example.shopapp.repository.UserRepository;
import com.example.shopapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of UserService interface.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Registers a new user.
     *
     * @param request the user registration request DTO
     * @return the user response DTO
     */
    @Override
    @Transactional
    public UserResponse register(UserRequestDTO request) {
        logger.info("Attempting to register user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = userMapper.toEntity(request);
        // Password hashing to be implemented later

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with id: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    /**
     * Authenticates a user.
     *
     * @param request the login request DTO
     * @return the user response DTO
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        logger.info("User login attempt with email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Password verification to be implemented later
        if (!request.getPassword().equals(user.getPassword())) {
            logger.warn("Invalid credentials for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        logger.info("User logged in successfully with id: {}", user.getId());
        return userMapper.toResponse(user);
    }

    /**
     * Updates user profile information.
     *
     * @param userId  the user ID
     * @param request the user update request DTO
     * @return the updated user response DTO
     */
    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, UserRequestDTO request) {
        logger.info("Updating profile for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already exists");
        }

        User updatedUser = userMapper.updateEntity(user, request);
        User savedUser = userRepository.save(updatedUser);
        logger.info("User profile updated successfully for id: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    /**
     * Finds a user by their ID.
     *
     * @param userId the user ID
     * @return the user entity
     */
    @Override
    @Transactional(readOnly = true)
    public User findUserById(UUID userId) {
        logger.info("Finding user by id: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}

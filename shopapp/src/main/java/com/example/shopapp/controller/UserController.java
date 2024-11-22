package com.example.shopapp.controller;

import com.example.shopapp.dto.request.LoginRequest;
import com.example.shopapp.dto.request.UpdateUserRequest;
import com.example.shopapp.dto.request.UserRequestDTO;
import com.example.shopapp.dto.response.UserResponse;
import com.example.shopapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for user-related operations.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Registers a new user.
     *
     * @param user the user registration request DTO
     * @return the user response DTO
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequestDTO user) {
        log.info("Registering user with email: {}", user.getEmail());
        UserResponse response = userService.register(user);
        log.info("User registered successfully with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticates a user.
     *
     * @param request the login request DTO
     * @return the user response DTO
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("User login attempt with identifier: {}", request.getIdentifier());
        UserResponse user = userService.login(request);
        log.info("User logged in successfully with ID: {}", user.getId());
        return ResponseEntity.ok(user);
    }

    /**
     * Updates user profile information.
     *
     * @param userId  the user ID
     * @param request the user update request DTO
     * @return the updated user response DTO
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        UserResponse response = userService.updateProfile(userId, request);
        log.info("User profile updated successfully for ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }
}

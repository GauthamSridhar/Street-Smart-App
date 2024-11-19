package com.example.shopapp.controller;

import com.example.shopapp.dto.request.LoginRequest;
import com.example.shopapp.dto.request.UserRequestDTO;
import com.example.shopapp.dto.response.UserResponse;
import com.example.shopapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * REST controller for user-related operations.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
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
        UserResponse response = userService.register(user);
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
        UserResponse user = userService.login(request);
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
            @Valid @RequestBody UserRequestDTO request) {
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }
}

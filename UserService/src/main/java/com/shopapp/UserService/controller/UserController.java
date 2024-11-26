package com.shopapp.UserService.controller;

import com.shopapp.UserService.dto.user.request.LoginRequest;
import com.shopapp.UserService.dto.user.request.RegisterUserRequest;
import com.shopapp.UserService.dto.user.request.UpdateUserRequest;
import com.shopapp.UserService.dto.user.response.UserResponse;
import com.shopapp.UserService.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        log.info("Registering user with email: {}", request.getEmail());
        UserResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("User login attempt with identifier: {}", request.getIdentifier());
        UserResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        log.info("Fetching user with ID: {}", userId);
        UserResponse response = userService.findUserById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> doesUserExist(@PathVariable UUID userId) {
        log.info("Checking if user exists with ID: {}", userId);
        boolean exists = userService.doesUserExist(userId);
        return ResponseEntity.ok(exists);
    }



}

package com.shopapp.UserService.controller;

import com.shopapp.UserService.dto.user.JwtToken;
import com.shopapp.UserService.dto.user.request.LoginRequest;
import com.shopapp.UserService.dto.user.request.RegisterUserRequest;
import com.shopapp.UserService.dto.user.request.UpdateUserRequest;
import com.shopapp.UserService.dto.user.response.UserResponse;
import com.shopapp.UserService.service.UserService;
import com.shopapp.UserService.service.impl.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        log.info("Registering user with email: {}", request.getEmail());
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@Valid @RequestBody LoginRequest request) {
        log.info("User login attempt with identifier: {}", request.getIdentifier());
        JwtToken jwtToken = authenticationService.authenticate(request);
        log.info("User logged in successfully with identifier: {}", request.getIdentifier());
        return ResponseEntity.ok(jwtToken);
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


    @PostMapping("/validate")
    public ResponseEntity<Void> validateToken(@RequestParam String token) {
        log.info("Validating token: {}", token);
        authenticationService.validateToken(token);
        return ResponseEntity.ok().build();
    }

}

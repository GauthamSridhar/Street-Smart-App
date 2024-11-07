package com.example.shopapp.controller;

import com.example.shopapp.dto.request.LoginRequest;
import com.example.shopapp.dto.request.UserRequestDTO;
import com.example.shopapp.dto.response.UserResponse;
import com.example.shopapp.model.User;
import com.example.shopapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequestDTO user) {
        return ResponseEntity.ok(userService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        UserResponse user = userService.login(request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable UUID userId,
            @RequestBody User user) {
        return ResponseEntity.ok(userService.updateProfile(userId, user));
    }
}

package com.shopapp.UserService.controller;

import com.shopapp.UserService.dto.user.*;
import com.shopapp.UserService.dto.user.request.*;
import com.shopapp.UserService.dto.user.response.UserResponse;
import com.shopapp.UserService.service.UserService;
import com.shopapp.UserService.service.impl.AuthenticationService;
import com.shopapp.common.Caller;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService users;
  private final AuthenticationService authentication;

  @PostMapping("/register")
  public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(users.register(request));
  }

  @PostMapping("/login")
  public JwtToken login(@Valid @RequestBody LoginRequest request) {
    return authentication.authenticate(request);
  }

  @GetMapping("/me")
  public UserResponse me() {
    return users.findUserById(Caller.id());
  }

  @GetMapping("/{userId}")
  public UserResponse getUser(@PathVariable UUID userId) {
    return users.findUserById(userId);
  }

  @PutMapping("/{userId}")
  public UserResponse updateProfile(
      @PathVariable UUID userId, @Valid @RequestBody UpdateUserRequest request) {
    return users.updateProfile(userId, request);
  }

  // Validation uses the Authorization header and the same resource-server checks as every other
  // request.
  @PostMapping("/validate")
  public ResponseEntity<Void> validateToken() {
    return ResponseEntity.noContent().build();
  }
}

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
  private final com.shopapp.UserService.service.impl.RefreshTokens refreshTokens;

  @org.springframework.beans.factory.annotation.Value("${security.refresh.secure-cookie:false}")
  private boolean secureCookie;

  @PostMapping("/register")
  public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(users.register(request));
  }

  @PostMapping("/login")
  public JwtToken login(
      @Valid @RequestBody LoginRequest request, jakarta.servlet.http.HttpServletResponse response) {
    var issued = authentication.authenticateWithRefresh(request);
    setRefreshCookie(response, issued.refreshToken());
    return issued.login();
  }

  @PostMapping("/refresh")
  public JwtToken refresh(
      @CookieValue(name = "ss_refresh", required = false) String token,
      @RequestHeader(name = "X-Requested-With", defaultValue = "") String requestedWith,
      jakarta.servlet.http.HttpServletResponse response) {
    if (!requestedWith.equals("StreetSmart"))
      throw new org.springframework.security.access.AccessDeniedException(
          "Refresh header required");
    try {
      var issued = refreshTokens.rotate(token);
      setRefreshCookie(response, issued.refreshToken());
      return issued.login();
    } catch (org.springframework.security.authentication.BadCredentialsException ex) {
      setRefreshCookie(response, "");
      throw ex;
    }
  }

  private void setRefreshCookie(jakarta.servlet.http.HttpServletResponse response, String token) {
    response.setHeader("Cache-Control", "no-store");
    response.addHeader(
        HttpHeaders.SET_COOKIE,
        ResponseCookie.from("ss_refresh", token)
            .httpOnly(true)
            .secure(secureCookie)
            .sameSite("Strict")
            .path("/api/users")
            .maxAge(
                token.isEmpty()
                    ? java.time.Duration.ZERO
                    : com.shopapp.UserService.service.impl.RefreshTokens.LIFETIME)
            .build()
            .toString());
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

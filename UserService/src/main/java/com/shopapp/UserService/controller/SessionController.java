package com.shopapp.UserService.controller;

import com.shopapp.UserService.repository.SessionRepository;
import com.shopapp.common.Caller;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SessionController {
  private final SessionRepository sessions;

  public record SessionResponse(UUID id, Instant createdAt, Instant expiresAt, boolean current) {}

  @GetMapping("/internal/sessions/{id}")
  @Transactional(readOnly = true)
  public boolean active(@PathVariable UUID id, @RequestParam UUID userId) {
    Caller.role("SERVICE");
    return sessions
        .findById(id)
        .filter(s -> s.getUserId().equals(userId) && s.getExpiresAt().isAfter(Instant.now()))
        .isPresent();
  }

  @PostMapping("/api/users/logout")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  @Transactional
  public void logout(@AuthenticationPrincipal Jwt jwt) {
    var id = UUID.fromString(jwt.getId());
    sessions
        .findById(id)
        .filter(s -> s.getUserId().equals(Caller.id()))
        .ifPresent(sessions::delete);
  }

  @GetMapping("/api/users/sessions")
  @Transactional(readOnly = true)
  public List<SessionResponse> mine(@AuthenticationPrincipal Jwt jwt) {
    var current = UUID.fromString(jwt.getId());
    return sessions
        .findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(Caller.id(), Instant.now())
        .stream()
        .map(
            s ->
                new SessionResponse(
                    s.getId(), s.getCreatedAt(), s.getExpiresAt(), s.getId().equals(current)))
        .toList();
  }

  @DeleteMapping("/api/users/sessions/{id}")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  @Transactional
  public void revoke(@PathVariable UUID id) {
    sessions
        .findById(id)
        .filter(s -> s.getUserId().equals(Caller.id()))
        .ifPresent(sessions::delete);
  }

  @PostMapping("/api/users/logout-all")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  @Transactional
  public void logoutAll() {
    sessions.deleteByUserId(Caller.id());
  }
}

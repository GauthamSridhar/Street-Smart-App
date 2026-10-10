package com.shopapp.UserService.service.impl;

import com.shopapp.UserService.dto.user.JwtToken;
import com.shopapp.UserService.dto.user.request.LoginRequest;
import com.shopapp.UserService.repository.UserRepository;
import com.shopapp.UserService.util.JwtUtil;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final UserRepository users;
  private final com.shopapp.UserService.repository.SessionRepository sessions;
  private final RefreshTokens refresh;

  @org.springframework.transaction.annotation.Transactional
  public JwtToken authenticate(LoginRequest credentials) {
    return authenticateWithRefresh(credentials).login();
  }

  @org.springframework.transaction.annotation.Transactional
  public RefreshTokens.Issued authenticateWithRefresh(LoginRequest credentials) {
    String identifier = credentials.getIdentifier().trim().toLowerCase(Locale.ROOT);
    var user =
        users
            .lockedByIdentifier(identifier)
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(identifier, credentials.getPassword()));
    var session = new com.shopapp.UserService.model.UserSession();
    session.setId(java.util.UUID.randomUUID());
    session.setUserId(user.getId());
    session.setCreatedAt(java.time.Instant.now());
    session.setExpiresAt(java.time.Instant.now().plus(RefreshTokens.LIFETIME));
    sessions.saveAndFlush(session);
    return new RefreshTokens.Issued(
        new JwtToken(
            jwtUtil.generateToken(user.getId(), user.getRole().name(), session.getId()),
            user.getEmail(),
            user.getRole().name(),
            user.getId().toString()),
        refresh.issue(session.getId()));
  }
}

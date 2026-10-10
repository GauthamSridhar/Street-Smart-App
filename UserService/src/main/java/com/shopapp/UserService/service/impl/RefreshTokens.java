package com.shopapp.UserService.service.impl;

import com.shopapp.UserService.dto.user.JwtToken;
import com.shopapp.UserService.model.*;
import com.shopapp.UserService.repository.*;
import com.shopapp.UserService.util.JwtUtil;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokens {
  public static final Duration LIFETIME = Duration.ofDays(7);
  private static final SecureRandom RANDOM = new SecureRandom();
  private final RefreshTokenRepository tokens;
  private final SessionRepository sessions;
  private final UserRepository users;
  private final JwtUtil jwt;

  public record Issued(JwtToken login, String refreshToken) {}

  public String issue(UUID sessionId) {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    var token = new RefreshToken();
    token.setTokenHash(hash(raw));
    token.setSessionId(sessionId);
    tokens.saveAndFlush(token);
    return raw;
  }

  // Reuse invalidates the entire session; that deletion must commit even on a 401.
  @Transactional(noRollbackFor = BadCredentialsException.class)
  public Issued rotate(String raw) {
    if (raw == null || !raw.matches("[A-Za-z0-9_-]{43}")) throw invalid();
    var token = tokens.locked(hash(raw)).orElseThrow(RefreshTokens::invalid);
    var session = sessions.locked(token.getSessionId()).orElseThrow(RefreshTokens::invalid);
    if (token.isConsumed() || !session.getExpiresAt().isAfter(Instant.now())) {
      sessions.delete(session);
      sessions.flush();
      throw invalid();
    }
    var user = users.findById(session.getUserId()).orElseThrow(RefreshTokens::invalid);
    token.setConsumed(true);
    tokens.saveAndFlush(token);
    return new Issued(
        new JwtToken(
            jwt.generateToken(user.getId(), user.getRole().name(), session.getId()),
            user.getEmail(),
            user.getRole().name(),
            user.getId().toString()),
        issue(session.getId()));
  }

  static String hash(String raw) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static BadCredentialsException invalid() {
    return new BadCredentialsException("Refresh session is invalid");
  }
}

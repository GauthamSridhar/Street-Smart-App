package com.shopapp.UserService.util;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  private final JwtEncoder encoder;
  private final String issuer;
  private final Duration ttl;

  public JwtUtil(
      @Value("${security.jwt.secret}") String secret,
      @Value("${security.jwt.issuer:street-smart}") String issuer,
      @Value("${security.jwt.ttl:PT1H}") Duration ttl) {
    byte[] bytes = Base64.getDecoder().decode(secret);
    if (bytes.length < 32) throw new IllegalArgumentException("JWT key is too short");
    if (ttl.isNegative() || ttl.isZero() || ttl.compareTo(Duration.ofHours(24)) > 0)
      throw new IllegalArgumentException("JWT lifetime must be between zero and 24 hours");
    this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(bytes));
    this.issuer = issuer;
    this.ttl = ttl;
  }

  public String generateToken(UUID id, String role) {
    return generateToken(id, role, UUID.randomUUID());
  }

  public String generateToken(UUID id, String role, UUID sessionId) {
    Instant now = Instant.now();
    var claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(id.toString())
            .audience(List.of("street-smart-api"))
            .issuedAt(now)
            .expiresAt(now.plus(ttl))
            .id(sessionId.toString())
            .claim("roles", List.of(role))
            .build();
    return encoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .getTokenValue();
  }
}

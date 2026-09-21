package com.shopapp.UserService;

import static org.assertj.core.api.Assertions.*;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JwtPolicyTests {
  @Autowired JwtDecoder decoder;

  String token(String issuer, String audience, Instant expiresAt) {
    var encoder =
        new NimbusJwtEncoder(
            new ImmutableSecret<>(
                Base64.getDecoder().decode("dGVzdC1vbmx5LXN0cmVldC1zbWFydC1rZXktMzItYnl0ZXM=")));
    var claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(UUID.randomUUID().toString())
            .audience(List.of(audience))
            .issuedAt(Instant.now().minusSeconds(3600))
            .expiresAt(expiresAt)
            .claim("roles", List.of("USER"))
            .build();
    return encoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .getTokenValue();
  }

  @Test
  void expiredTokenIsRejectedBeyondAllowedClockSkew() {
    assertThatThrownBy(
            () ->
                decoder.decode(
                    token("street-smart", "street-smart-api", Instant.now().minusSeconds(120))))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void wrongIssuerIsRejected() {
    assertThatThrownBy(
            () ->
                decoder.decode(
                    token("another-issuer", "street-smart-api", Instant.now().plusSeconds(600))))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void wrongAudienceIsRejected() {
    assertThatThrownBy(
            () ->
                decoder.decode(
                    token("street-smart", "another-api", Instant.now().plusSeconds(600))))
        .isInstanceOf(JwtException.class);
  }
}

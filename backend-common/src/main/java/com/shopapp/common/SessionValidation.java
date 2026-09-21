package com.shopapp.common;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SessionValidation implements OAuth2TokenValidator<Jwt> {
  private final RestClient client;
  private final String url;

  public SessionValidation(
      @Value("${security.sessions.url:}") String url,
      @Value("${security.internal-key}") String key) {
    this.url = url;
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(2));
    factory.setReadTimeout(Duration.ofSeconds(2));
    client =
        RestClient.builder().requestFactory(factory).defaultHeader("X-Internal-Key", key).build();
  }

  public OAuth2TokenValidatorResult validate(Jwt jwt) {
    if (url.isBlank()) return OAuth2TokenValidatorResult.success();
    try {
      var id = java.util.UUID.fromString(jwt.getId());
      var user = java.util.UUID.fromString(jwt.getSubject());
      if (Boolean.TRUE.equals(
          client.get().uri(url + "/{id}?userId={user}", id, user).retrieve().body(Boolean.class)))
        return OAuth2TokenValidatorResult.success();
    } catch (RuntimeException ignored) {
      /* Fail closed when the authoritative session store is unavailable. */
    }
    return OAuth2TokenValidatorResult.failure(
        new OAuth2Error("invalid_token", "Session is no longer valid", null));
  }
}

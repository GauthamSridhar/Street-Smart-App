package com.shopapp.common;

import static org.assertj.core.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class SessionValidationTests {
  @Test
  void revocationAndStoreFailureDenyAccess() throws Exception {
    var body = new AtomicReference<>("true");
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/internal/sessions",
        exchange -> {
          byte[] bytes = body.get().getBytes(java.nio.charset.StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          exchange.getResponseBody().write(bytes);
          exchange.close();
        });
    server.start();
    var validator =
        new SessionValidation(
            "http://127.0.0.1:" + server.getAddress().getPort() + "/internal/sessions", "test-key");
    var jwt =
        Jwt.withTokenValue("test")
            .header("alg", "HS256")
            .subject(UUID.randomUUID().toString())
            .claim("jti", UUID.randomUUID().toString())
            .build();
    try {
      assertThat(validator.validate(jwt).hasErrors()).isFalse();
      body.set("false");
      assertThat(validator.validate(jwt).hasErrors()).isTrue();
    } finally {
      server.stop(0);
    }
    assertThat(validator.validate(jwt).hasErrors()).isTrue();
  }
}

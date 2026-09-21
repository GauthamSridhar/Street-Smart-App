package com.shopapp.DiscoveryClient;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GatewaySecurityTests {
  WebTestClient client;
  @org.springframework.boot.test.web.server.LocalServerPort int port;

  @org.junit.jupiter.api.BeforeEach
  void connect() {
    client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  @Test
  void protectedRoutesRequireToken() {
    client.get().uri("/api/shops").exchange().expectStatus().isUnauthorized();
    client
        .get()
        .uri("/api/shops")
        .header("Authorization", "Bearer invalid")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void internalRoutesCannotBeCalledThroughGateway() {
    client
        .post()
        .uri("/internal/approvals/123")
        .header("X-Internal-Key", "anything")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void onlyConfiguredOriginsPassPreflight() {
    client
        .options()
        .uri("/api/shops")
        .header("Origin", "http://localhost:4200")
        .header("Access-Control-Request-Method", "GET")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .valueEquals("Access-Control-Allow-Origin", "http://localhost:4200");
    client
        .options()
        .uri("/api/shops")
        .header("Origin", "https://untrusted.example")
        .header("Access-Control-Request-Method", "GET")
        .exchange()
        .expectStatus()
        .isForbidden();
  }
}

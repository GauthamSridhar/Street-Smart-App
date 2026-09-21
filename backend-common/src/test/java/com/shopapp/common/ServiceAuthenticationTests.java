package com.shopapp.common;

import static org.assertj.core.api.Assertions.*;

import feign.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.*;

class ServiceAuthenticationTests {
  interface Endpoint {
    @RequestLine("PUT /{id}/approval")
    void call(@Param("id") String id);
  }

  private final String key = "test-only-internal-key-32-characters-long";

  @AfterEach
  void clean() {
    RequestContextHolder.resetRequestAttributes();
  }

  Endpoint endpoint(String url, java.util.function.Consumer<Request> check) {
    return Feign.builder()
        .requestInterceptor(new BackendConfiguration().serviceAuthentication(key))
        .client(
            (request, options) -> {
              check.accept(request);
              return Response.builder()
                  .request(request)
                  .status(200)
                  .reason("OK")
                  .headers(Map.of())
                  .body("", StandardCharsets.UTF_8)
                  .build();
            })
        .target(Endpoint.class, url);
  }

  @Test
  void authenticatesInternalPathInFeignTargetBeforeTargetIsApplied() {
    endpoint(
            "http://ShopService/internal/shops",
            request -> assertThat(request.headers().get("X-Internal-Key")).containsExactly(key))
        .call("id");
  }

  @Test
  void publicClientForwardsBearerWithoutInternalCredential() {
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer user-token");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    endpoint(
            "http://ShopService/api/shops",
            outgoing -> {
              assertThat(outgoing.headers()).doesNotContainKey("X-Internal-Key");
              assertThat(outgoing.headers().get("Authorization"))
                  .containsExactly("Bearer user-token");
            })
        .call("id");
  }
}

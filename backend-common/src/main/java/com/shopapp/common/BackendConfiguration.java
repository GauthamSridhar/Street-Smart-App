package com.shopapp.common;

import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class BackendConfiguration {
  @Bean
  public Retryer feignRetryer() {
    return Retryer.NEVER_RETRY;
  }

  @Bean
  public RequestInterceptor serviceAuthentication(@Value("${security.internal-key}") String key) {
    return request -> {
      String requestId = org.slf4j.MDC.get("requestId");
      if (requestId != null) request.header("X-Request-ID", requestId);
      // Feign applies the target's base path AFTER request interceptors.
      String basePath =
          request.feignTarget() == null
              ? ""
              : java.net.URI.create(request.feignTarget().url()).getPath();
      if (basePath.startsWith("/internal/") || request.path().startsWith("/internal/"))
        request.header("X-Internal-Key", key);
      else if (RequestContextHolder.getRequestAttributes()
          instanceof ServletRequestAttributes attrs) {
        String token = attrs.getRequest().getHeader("Authorization");
        if (token != null) request.header("Authorization", token);
      }
    };
  }
}

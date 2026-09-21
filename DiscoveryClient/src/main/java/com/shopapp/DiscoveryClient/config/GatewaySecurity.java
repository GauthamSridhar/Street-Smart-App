package com.shopapp.DiscoveryClient.config;

import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.*;

@Configuration
public class GatewaySecurity {
  @Bean
  public ReactiveJwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secret) {
    byte[] key = Base64.getDecoder().decode(secret);
    if (key.length < 32)
      throw new IllegalArgumentException("JWT_SECRET must contain at least 32 random bytes");
    var decoder =
        NimbusReactiveJwtDecoder.withSecretKey(new SecretKeySpec(key, "HmacSHA256"))
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer("street-smart"),
            jwt ->
                jwt.getAudience().contains("street-smart-api")
                        && jwt.getClaimAsStringList("roles") != null
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"))));
    return decoder;
  }

  @Bean
  public SecurityWebFilterChain security(
      ServerHttpSecurity http,
      @org.springframework.beans.factory.annotation.Qualifier("cors")
          CorsConfigurationSource source) {
    return http.csrf(c -> c.disable())
        .cors(c -> c.configurationSource(source))
        .authorizeExchange(
            a ->
                a.pathMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .pathMatchers(
                        "/actuator/health",
                        "/actuator/health/**",
                        "/api/users/login",
                        "/api/users/register",
                        "/api/sms/send",
                        "/api/sms/verify",
                        "/api/sms/config")
                    .permitAll()
                    .pathMatchers("/internal/**")
                    .denyAll()
                    .pathMatchers("/api/**")
                    .authenticated()
                    .anyExchange()
                    .denyAll())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (exchange, ex) -> problem(exchange, 401, "Authentication required"))
                    .accessDeniedHandler((exchange, ex) -> problem(exchange, 403, "Access denied")))
        .oauth2ResourceServer(
            o ->
                o.jwt(j -> {})
                    .authenticationEntryPoint(
                        (exchange, ex) -> problem(exchange, 401, "Authentication required")))
        .build();
  }

  private static reactor.core.publisher.Mono<Void> problem(
      org.springframework.web.server.ServerWebExchange exchange, int status, String detail) {
    var response = exchange.getResponse();
    response.setStatusCode(org.springframework.http.HttpStatus.valueOf(status));
    response
        .getHeaders()
        .setContentType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON);
    if (status == 401) response.getHeaders().set("WWW-Authenticate", "Bearer");
    byte[] json =
        ("{\"status\":" + status + ",\"detail\":\"" + detail + "\"}")
            .getBytes(java.nio.charset.StandardCharsets.UTF_8);
    return response.writeWith(
        reactor.core.publisher.Mono.just(response.bufferFactory().wrap(json)));
  }

  @Bean
  public CorsConfigurationSource cors(@Value("${security.allowed-origins}") String origins) {
    var config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
    config.setExposedHeaders(List.of("X-Request-ID"));
    config.setMaxAge(3600L);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}

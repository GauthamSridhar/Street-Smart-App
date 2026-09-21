package com.shopapp.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableMethodSecurity
public class BackendSecurity {
  @Bean
  public JwtDecoder jwtDecoder(
      @Value("${security.jwt.secret}") String encoded,
      @Value("${security.jwt.issuer:street-smart}") String issuer,
      SessionValidation sessions) {
    byte[] key = Base64.getDecoder().decode(encoded);
    if (key.length < 32)
      throw new IllegalArgumentException(
          "JWT_SECRET must contain at least 32 random bytes, Base64 encoded");
    var decoder =
        NimbusJwtDecoder.withSecretKey(new SecretKeySpec(key, "HmacSHA256"))
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(issuer),
            sessions,
            jwt ->
                jwt.getAudience().contains("street-smart-api")
                        && jwt.getClaimAsStringList("roles") != null
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"))));
    return decoder;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, @Value("${security.internal-key}") String internalKey) throws Exception {
    if (internalKey.length() < 32)
      throw new IllegalArgumentException("INTERNAL_API_KEY must be at least 32 characters");
    var authorities = new JwtGrantedAuthoritiesConverter();
    authorities.setAuthoritiesClaimName("roles");
    authorities.setAuthorityPrefix("ROLE_");
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authorities);
    http.csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/actuator/health", "/actuator/health/**")
                    .permitAll()
                    .requestMatchers(
                        "/api/users/register",
                        "/api/users/login",
                        "/api/sms/send",
                        "/api/sms/verify",
                        "/api/sms/config")
                    .permitAll()
                    .requestMatchers("/internal/**")
                    .hasRole("SERVICE")
                    .requestMatchers(
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            o ->
                o.jwt(j -> j.jwtAuthenticationConverter(converter))
                    .authenticationEntryPoint(
                        (req, res, ex) -> error(res, 401, "Authentication required")))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (req, res, ex) -> error(res, 401, "Authentication required"))
                    .accessDeniedHandler((req, res, ex) -> error(res, 403, "Access denied")))
        .addFilterBefore(
            new OncePerRequestFilter() {
              @Override
              protected void doFilterInternal(
                  HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                  throws IOException, ServletException {
                if (req.getRequestURI().startsWith("/internal/")) {
                  String supplied = req.getHeader("X-Internal-Key");
                  if (supplied == null
                      || !MessageDigest.isEqual(
                          internalKey.getBytes(StandardCharsets.UTF_8),
                          supplied.getBytes(StandardCharsets.UTF_8))) {
                    error(res, 403, "Internal service authentication required");
                    return;
                  }
                  var context = SecurityContextHolder.createEmptyContext();
                  context.setAuthentication(
                      new UsernamePasswordAuthenticationToken(
                          "internal-service",
                          null,
                          List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))));
                  SecurityContextHolder.setContext(context);
                }
                chain.doFilter(req, res);
              }
            },
            BearerTokenAuthenticationFilter.class);
    return http.build();
  }

  static void error(HttpServletResponse res, int status, String message) throws IOException {
    res.setStatus(status);
    res.setContentType("application/problem+json");
    res.getWriter().write("{\"status\":" + status + ",\"detail\":\"" + message + "\"}");
  }
}

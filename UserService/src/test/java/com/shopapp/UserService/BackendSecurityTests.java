package com.shopapp.UserService;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.shopapp.UserService.model.*;
import com.shopapp.UserService.repository.UserRepository;
import com.shopapp.UserService.util.JwtUtil;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendSecurityTests {
  @Autowired MockMvc mvc;

  @Autowired UserRepository users;
  @Autowired JwtUtil tokens;
  @Autowired JwtDecoder decoder;
  @Autowired BCryptPasswordEncoder passwords;

  @BeforeEach
  void clean() {
    users.deleteAll();
  }

  String registration(String role) {
    return "{\"email\":\"alice@example.test\",\"password\":\"a-strong-password\",\"fullName\":\"Alice\",\"phoneNumber\":\"+919876543210\",\"role\":\""
        + role
        + "\"}";
  }

  @Test
  void registrationCannotCreateAdmin() throws Exception {
    mvc.perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registration("ADMIN")))
        .andExpect(status().isForbidden());
    assertThat(users.count()).isZero();
  }

  @Test
  void registerLoginAndReadOwnProfileUsingSignedToken() throws Exception {
    mvc.perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registration("USER")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.password").doesNotExist());
    var user = users.findByEmail("alice@example.test").orElseThrow();
    assertThat(passwords.matches("a-strong-password", user.getPassword())).isTrue();
    var result =
        mvc.perform(
                post("/api/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"identifier\":\"ALICE@example.test\",\"password\":\"a-strong-password\"}"))
            .andExpect(status().isOk())
            .andReturn();
    String token =
        new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(result.getResponse().getContentAsString())
            .get("jwt")
            .asText();
    assertThat(decoder.decode(token).getSubject()).isEqualTo(user.getId().toString());
    mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("alice@example.test"));
  }

  @Test
  void cannotReadAnotherUsersProfile() throws Exception {
    mvc.perform(
            get("/api/users/" + UUID.randomUUID())
                .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()))))
        .andExpect(status().isForbidden());
  }

  @Test
  void cannotOverwriteAnotherUsersProfile() throws Exception {
    mvc.perform(
            put("/api/users/" + UUID.randomUUID())
                .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"email\":\"alice@example.test\",\"fullName\":\"Alice\",\"phoneNumber\":\"+919876543210\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void unauthorizedAndMalformedTokensAreRejected() throws Exception {
    mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/users/me").header("Authorization", "Bearer invalid"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void invalidInputAndDuplicateAccountsHaveStableErrors() throws Exception {
    mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").exists());
    mvc.perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registration("USER")))
        .andExpect(status().isCreated());
    mvc.perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registration("USER")))
        .andExpect(status().isConflict());
  }

  @Test
  void smsCannotRelayArbitraryMessages() throws Exception {
    mvc.perform(
            post("/api/sms/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"phoneNumber\":\"+919876543210\",\"message\":\"arbitrary\",\"otpCode\":\"123456\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/sms/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phoneNumber\":\"+919876543210\"}"))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void signingKeySurvivesNewIssuerInstance() {
    UUID id = UUID.randomUUID();
    var restarted =
        new JwtUtil(
            "dGVzdC1vbmx5LXN0cmVldC1zbWFydC1rZXktMzItYnl0ZXM=",
            "street-smart",
            java.time.Duration.ofHours(1));
    assertThat(decoder.decode(restarted.generateToken(id, "USER")).getSubject())
        .isEqualTo(id.toString());
  }

  @Test
  void wrongKeyIsRejected() {
    var other =
        new JwtUtil(
            java.util.Base64.getEncoder()
                .encodeToString("another-test-only-key-32-bytes-long".getBytes()),
            "street-smart",
            java.time.Duration.ofHours(1));
    assertThatThrownBy(() -> decoder.decode(other.generateToken(UUID.randomUUID(), "ADMIN")))
        .isInstanceOf(org.springframework.security.oauth2.jwt.JwtException.class);
  }
}

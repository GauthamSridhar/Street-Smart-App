package com.shopapp.UserService;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopapp.UserService.repository.*;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RefreshTokenTests {
  @Autowired MockMvc mvc;
  @Autowired UserRepository users;
  @Autowired SessionRepository sessions;
  @Autowired RefreshTokenRepository refreshTokens;
  @Autowired JwtDecoder decoder;

  @BeforeEach
  void clean() {
    users.deleteAll();
  }

  private record Login(String jwt, Cookie cookie) {}

  private Login login() throws Exception {
    mvc.perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"email\":\"refresh@example.test\",\"password\":\"strong-password-2026\",\"fullName\":\"Refresh Test\",\"phoneNumber\":\"+919876543211\",\"role\":\"USER\"}"))
        .andExpect(status().isCreated());
    var response =
        mvc.perform(
                post("/api/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"identifier\":\"refresh@example.test\",\"password\":\"strong-password-2026\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    assertThat(response.getHeader("Set-Cookie"))
        .contains("HttpOnly", "SameSite=Strict", "Path=/api/users");
    return new Login(
        new ObjectMapper().readTree(response.getContentAsString()).get("jwt").asText(),
        response.getCookie("ss_refresh"));
  }

  @Test
  void rotatesHashedSecretsAndReuseRevokesTheSession() throws Exception {
    var login = login();
    assertThat(refreshTokens.findAll().get(0).getTokenHash())
        .hasSize(64)
        .isNotEqualTo(login.cookie().getValue());
    mvc.perform(post("/api/users/refresh").cookie(login.cookie()))
        .andExpect(status().isForbidden());
    var response =
        mvc.perform(
                post("/api/users/refresh")
                    .cookie(login.cookie())
                    .header("X-Requested-With", "StreetSmart"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    var replacement = response.getCookie("ss_refresh");
    assertThat(replacement.getValue()).isNotEqualTo(login.cookie().getValue());
    mvc.perform(
            post("/api/users/refresh")
                .cookie(login.cookie())
                .header("X-Requested-With", "StreetSmart"))
        .andExpect(status().isUnauthorized());
    assertThat(sessions.count()).isZero();
    assertThat(refreshTokens.count()).isZero();
    mvc.perform(
            post("/api/users/refresh")
                .cookie(replacement)
                .header("X-Requested-With", "StreetSmart"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void expiryAndLogoutPreventRenewal() throws Exception {
    var login = login();
    var session = sessions.findAll().get(0);
    session.setExpiresAt(Instant.now().minusSeconds(1));
    sessions.saveAndFlush(session);
    mvc.perform(
            post("/api/users/refresh")
                .cookie(login.cookie())
                .header("X-Requested-With", "StreetSmart"))
        .andExpect(status().isUnauthorized());
    users.deleteAll();
    login = login();
    mvc.perform(post("/api/users/logout").header("Authorization", "Bearer " + login.jwt()))
        .andExpect(status().isNoContent());
    mvc.perform(
            post("/api/users/refresh")
                .cookie(login.cookie())
                .header("X-Requested-With", "StreetSmart"))
        .andExpect(status().isUnauthorized());
  }
}

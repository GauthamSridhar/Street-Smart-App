package com.shopapp.UserService;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopapp.UserService.model.*;
import com.shopapp.UserService.repository.*;
import com.shopapp.UserService.service.impl.SharedAuthQuota;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SessionLifecycleTests {
  @Autowired MockMvc mvc;
  @Autowired UserRepository users;
  @Autowired SessionRepository sessions;
  @Autowired BCryptPasswordEncoder passwords;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;
  @Autowired SharedAuthQuota quota;

  @Test
  void loginCreatesSessionAndLogoutRemovesIt() throws Exception {
    jdbc.update("update auth_quotas set hits=0,window_start=0");
    var user = new User();
    user.setEmail(UUID.randomUUID() + "@example.test");
    user.setPhoneNumber("+919111111112");
    user.setFullName("Session test");
    user.setPassword(passwords.encode("strong-password-test"));
    user.setRole(UserRole.USER);
    user = users.saveAndFlush(user);
    String response =
        mvc.perform(
                post("/api/users/login")
                    .contentType("application/json")
                    .content(
                        json.writeValueAsString(
                            java.util.Map.of(
                                "identifier",
                                user.getEmail(),
                                "password",
                                "strong-password-test"))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String token = json.readTree(response).get("jwt").asText();
    var id = UUID.fromString(com.nimbusds.jwt.SignedJWT.parse(token).getJWTClaimsSet().getJWTID());
    assertThat(sessions.existsById(id)).isTrue();
    mvc.perform(get("/api/users/sessions").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].current").value(true));
    var second = new UserSession();
    second.setId(UUID.randomUUID());
    second.setUserId(user.getId());
    second.setCreatedAt(java.time.Instant.now());
    second.setExpiresAt(java.time.Instant.now().plusSeconds(3600));
    sessions.saveAndFlush(second);
    mvc.perform(
            delete("/api/users/sessions/" + second.getId())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());
    assertThat(sessions.existsById(second.getId())).isFalse();
    mvc.perform(post("/api/users/logout").header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());
    assertThat(sessions.existsById(id)).isFalse();
    sessions.saveAndFlush(second);
    mvc.perform(
            post("/api/users/logout-all")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(j -> j.subject(second.getUserId().toString()))))
        .andExpect(status().isNoContent());
    assertThat(sessions.existsById(second.getId())).isFalse();
    users.deleteById(user.getId());
  }

  @Test
  void quotaIsSharedAndResetsAtWindowBoundary() {
    jdbc.update("update auth_quotas set hits=0,window_start=0 where path='/api/sms/send'");
    try {
      for (int i = 0; i < 30; i++) assertThat(quota.allow("/api/sms/send")).isTrue();
      assertThat(quota.allow("/api/sms/send")).isFalse();
      jdbc.update("update auth_quotas set window_start=0 where path='/api/sms/send'");
      assertThat(quota.allow("/api/sms/send")).isTrue();
    } finally {
      jdbc.update("update auth_quotas set hits=0,window_start=0");
    }
  }
}

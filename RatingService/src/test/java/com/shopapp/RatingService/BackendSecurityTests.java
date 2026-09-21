package com.shopapp.RatingService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.shopapp.RatingService.dto.rating.ShopResponse;
import com.shopapp.RatingService.feign.ShopFeignClient;
import com.shopapp.RatingService.repository.RatingRepository;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendSecurityTests {
  @Autowired MockMvc mvc;

  @Autowired RatingRepository ratings;
  @Autowired com.shopapp.RatingService.repository.ReviewReportRepository reports;
  @MockitoBean ShopFeignClient shops;
  UUID user, shopId;

  @BeforeEach
  void clean() {
    reports.deleteAll();
    ratings.deleteAll();
    user = UUID.randomUUID();
    shopId = UUID.randomUUID();
    var shop = new ShopResponse();
    shop.setId(shopId);
    shop.setOwnerId(UUID.randomUUID());
    shop.setStatus("APPROVED");
    when(shops.getShop(shopId)).thenReturn(shop);
  }

  org.springframework.test.web.servlet.request.RequestPostProcessor customer(UUID id) {
    return jwt()
        .jwt(j -> j.subject(id.toString()))
        .authorities(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @Test
  void reportsEnforceOwnershipAndAdminDecisionsRetainAudit() throws Exception {
    for (String action : List.of("DISMISS", "REMOVE")) {
      var rating = new com.shopapp.RatingService.model.Rating();
      rating.setUserId(UUID.randomUUID());
      rating.setShopId(shopId);
      rating.setRating(3);
      rating.setReview("Content to moderate");
      rating = ratings.saveAndFlush(rating);
      String path = "/api/ratings/" + rating.getId() + "/reports";
      mvc.perform(
              post(path)
                  .with(customer(rating.getUserId()))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"reason\":\"Spam\"}"))
          .andExpect(status().isConflict());
      mvc.perform(
              post(path)
                  .with(customer(user))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"reason\":\"Spam\"}"))
          .andExpect(status().isOk());
      mvc.perform(
              post(path)
                  .with(customer(user))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"reason\":\"Spam\"}"))
          .andExpect(status().isConflict());
      var report =
          reports.findAll().stream()
              .filter(r -> r.getStatus().equals("OPEN"))
              .findFirst()
              .orElseThrow();
      String resolve = "/api/ratings/reports/" + report.getId() + "/resolve";
      mvc.perform(get("/api/ratings/reports").with(customer(user)))
          .andExpect(status().isForbidden());
      mvc.perform(
              post(resolve)
                  .with(customer(user))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"action\":\"REMOVE\"}"))
          .andExpect(status().isForbidden());
      UUID adminId = UUID.randomUUID();
      var admin =
          jwt()
              .jwt(j -> j.subject(adminId.toString()))
              .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
      mvc.perform(post(resolve).with(admin).contentType(MediaType.APPLICATION_JSON).content("{}"))
          .andExpect(status().isBadRequest());
      mvc.perform(
              post(resolve)
                  .with(admin)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"action\":\"" + action + "\"}"))
          .andExpect(status().isNoContent());
      var saved = reports.findById(report.getId()).orElseThrow();
      assertThat(saved.getResolvedBy()).isEqualTo(adminId);
      assertThat(saved.getResolvedAt()).isNotNull();
      assertThat(saved.getReviewSnapshot()).isEqualTo("Content to moderate");
      assertThat(ratings.existsById(rating.getId())).isEqualTo(action.equals("DISMISS"));
      mvc.perform(
              post(resolve)
                  .with(admin)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"action\":\"DISMISS\"}"))
          .andExpect(status().isConflict());
    }
  }

  @Test
  void ratingCannotImpersonateAnotherUser() throws Exception {
    mvc.perform(
            post("/api/ratings/add")
                .param("userId", UUID.randomUUID().toString())
                .param("shopId", shopId.toString())
                .with(customer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":5}"))
        .andExpect(status().isForbidden());
    assertThat(ratings.count()).isZero();
  }

  @Test
  void ownReviewLookupUsesSignedIdentity() throws Exception {
    mvc.perform(
            post("/api/ratings/add")
                .param("userId", user.toString())
                .param("shopId", shopId.toString())
                .with(customer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":5}"))
        .andExpect(status().isOk());
    mvc.perform(get("/api/ratings/mine/" + shopId).with(customer(user)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(user.toString()));
    mvc.perform(get("/api/ratings/mine/" + shopId).with(customer(UUID.randomUUID())))
        .andExpect(status().isNotFound());
  }

  @Test
  void invalidRatingIsRejectedAndDuplicateIsAConflict() throws Exception {
    mvc.perform(
            post("/api/ratings/add")
                .param("userId", user.toString())
                .param("shopId", shopId.toString())
                .with(customer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":6}"))
        .andExpect(status().isBadRequest());
    for (int i = 0; i < 2; i++)
      mvc.perform(
              post("/api/ratings/add")
                  .param("userId", user.toString())
                  .param("shopId", shopId.toString())
                  .with(customer(user))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"rating\":5,\"review\":\"Helpful staff\"}"))
          .andExpect(status().is(i == 0 ? 200 : 409));
    assertThat(ratings.count()).isEqualTo(1);
  }

  @Test
  void deleteMustMatchStoredOwnerAndShop() throws Exception {
    mvc.perform(
            post("/api/ratings/add")
                .param("userId", user.toString())
                .param("shopId", shopId.toString())
                .with(customer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":4}"))
        .andExpect(status().isOk());
    UUID id = ratings.findAll().get(0).getId(), other = UUID.randomUUID();
    mvc.perform(
            delete("/api/ratings/" + id)
                .param("userId", other.toString())
                .param("shopId", shopId.toString())
                .with(customer(other)))
        .andExpect(status().isForbidden());
    mvc.perform(
            delete("/api/ratings/" + id)
                .param("userId", user.toString())
                .param("shopId", UUID.randomUUID().toString())
                .with(customer(user)))
        .andExpect(status().isBadRequest());
    assertThat(ratings.count()).isEqualTo(1);
  }
}

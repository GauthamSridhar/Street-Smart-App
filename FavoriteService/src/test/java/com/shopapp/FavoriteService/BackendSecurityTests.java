package com.shopapp.FavoriteService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.shopapp.FavoriteService.dto.favourite.ShopBasicInfoDTO;
import com.shopapp.FavoriteService.feign.ShopFeignClient;
import com.shopapp.FavoriteService.repository.FavouriteRepository;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendSecurityTests {
  @Autowired MockMvc mvc;

  @Autowired FavouriteRepository favorites;
  @MockitoBean ShopFeignClient shops;
  UUID user, shopId;

  @BeforeEach
  void clean() {
    favorites.deleteAll();
    user = UUID.randomUUID();
    shopId = UUID.randomUUID();
    var shop = new ShopBasicInfoDTO();
    shop.setId(shopId);
    shop.setName("Neighbourhood Shop");
    when(shops.getShopBasicInfo(shopId)).thenReturn(shop);
  }

  org.springframework.test.web.servlet.request.RequestPostProcessor customer(UUID id) {
    return jwt()
        .jwt(j -> j.subject(id.toString()))
        .authorities(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @Test
  void addingTwiceIsIdempotentAndReturnsPersistentId() throws Exception {
    for (int i = 0; i < 2; i++)
      mvc.perform(
              post("/api/favorites/" + shopId)
                  .param("userId", user.toString())
                  .with(customer(user)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").isNotEmpty());
    assertThat(favorites.count()).isEqualTo(1);
    mvc.perform(get("/api/favorites/user/" + user).with(customer(user)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(favorites.findAll().get(0).getId().toString()));
  }

  @Test
  void customerCannotAccessOrAlterAnotherUsersFavorites() throws Exception {
    UUID other = UUID.randomUUID();
    mvc.perform(
            post("/api/favorites/" + shopId).param("userId", other.toString()).with(customer(user)))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/favorites/user/" + other).with(customer(user)))
        .andExpect(status().isForbidden());
    mvc.perform(
            delete("/api/favorites/" + shopId)
                .param("userId", other.toString())
                .with(customer(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void removalIsIdempotentAndCountUsesOwnedRecords() throws Exception {
    mvc.perform(
            post("/api/favorites/" + shopId).param("userId", user.toString()).with(customer(user)))
        .andExpect(status().isOk());
    for (int i = 0; i < 2; i++)
      mvc.perform(
              delete("/api/favorites/" + shopId)
                  .param("userId", user.toString())
                  .with(customer(user)))
          .andExpect(status().isNoContent());
    mvc.perform(get("/api/favorites/count/" + user).with(customer(user)))
        .andExpect(status().isOk())
        .andExpect(content().string("0"));
  }
}

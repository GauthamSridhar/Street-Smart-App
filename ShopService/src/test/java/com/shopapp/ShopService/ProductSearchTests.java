package com.shopapp.ShopService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.shopapp.ShopService.model.*;
import com.shopapp.ShopService.repository.*;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductSearchTests {
  @Autowired MockMvc mvc;
  @Autowired ShopRepository shops;
  @Autowired ProductRepository products;

  @BeforeEach
  void seed() {
    shops.deleteAll();
    add("Corner Store", "Grocery", ShopStatus.APPROVED, "Fresh Milk", true);
    add("Market", "Grocery", ShopStatus.ACTIVE, "Oat Milk", false);
    add("Milk Shop", "Books", ShopStatus.APPROVED, "Java Guide", true);
    add("Hidden", "Grocery", ShopStatus.PENDING, "Milk", true);
    add("Rejected", "Grocery", ShopStatus.REJECTED, "Milk", true);
    add("Discount Store", "Grocery", ShopStatus.INACTIVE, "100% Juice", true);
  }

  void add(String name, String category, ShopStatus status, String product, boolean available) {
    var s = new Shop();
    s.setName(name);
    s.setDescription("Test catalogue");
    s.setCategory(category);
    s.setAddress("Test Road");
    s.setLatitude(10.0);
    s.setLongitude(76.0);
    s.setStatus(status);
    s.setOwnerId(UUID.randomUUID());
    s = shops.saveAndFlush(s);
    var p = new Product();
    p.setShop(s);
    p.setName(product);
    p.setAvailable(available);
    products.saveAndFlush(p);
  }

  @Test
  void searchRequiresAuthentication() throws Exception {
    mvc.perform(get("/api/products/search")).andExpect(status().isUnauthorized());
  }

  @Test
  void optionalTypoToleranceIsCombinedWithVisibilityAndAvailability() throws Exception {
    mvc.perform(
            get("/api/products/search").param("q", "Fresh Mlik").param("fuzzy", "true").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1));
    mvc.perform(get("/api/products/search").param("q", "Fresh Mlik").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0));
    mvc.perform(get("/api/products/search").param("q", "a").param("fuzzy", "true").with(jwt()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void radiusFiltersAndValidatesCoordinates() throws Exception {
    mvc.perform(
            get("/api/products/search")
                .param("latitude", "10")
                .param("longitude", "76")
                .param("radiusKm", "1")
                .with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(3));
    mvc.perform(
            get("/api/products/search")
                .param("latitude", "40")
                .param("longitude", "76")
                .param("radiusKm", "1")
                .with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0));
    mvc.perform(get("/api/products/search").param("latitude", "NaN").with(jwt()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void priceFilterDefaultsToInrAndExcludesUnknownPrices() throws Exception {
    mvc.perform(get("/api/products/search").param("minPrice", "1").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0));
    mvc.perform(
            get("/api/products/search").param("minPrice", "1").param("currency", "INR").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  void matchesProductNotShopNameAndExcludesHiddenAndUnavailable() throws Exception {
    mvc.perform(get("/api/products/search").param("q", " MILK ").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].name").value("Fresh Milk"))
        .andExpect(jsonPath("$.content[0].shopName").value("Corner Store"));
  }

  @Test
  void categoryAndTextAreCombined() throws Exception {
    mvc.perform(
            get("/api/products/search").param("q", "milk").param("category", "Books").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  void availabilityCanBeIncludedAndResultsArePaginated() throws Exception {
    mvc.perform(
            get("/api/products/search")
                .param("q", "milk")
                .param("availableOnly", "false")
                .param("size", "1")
                .param("page", "1")
                .with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.content[0].name").value("Oat Milk"));
  }

  @Test
  void wildcardCharactersAreLiteralAndClosedShopIsLabelled() throws Exception {
    mvc.perform(get("/api/products/search").param("q", "%").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].shopStatus").value("INACTIVE"));
  }

  @Test
  void paginationIsBounded() throws Exception {
    mvc.perform(get("/api/products/search").param("size", "101").with(jwt()))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/products/search").param("page", "-1").with(jwt()))
        .andExpect(status().isBadRequest());
  }
}

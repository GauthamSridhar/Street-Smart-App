package com.shopapp.ShopService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.shopapp.ShopService.feign.ShopApprovalFeignClient;
import com.shopapp.ShopService.model.*;
import com.shopapp.ShopService.repository.*;
import com.shopapp.ShopService.service.ApprovalRequestDelivery;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendSecurityTests {
  @Autowired MockMvc mvc;

  @Autowired ShopRepository shops;
  @Autowired ProductRepository products;
  @Autowired ApprovalRequestDelivery delivery;
  @MockitoBean ShopApprovalFeignClient approvals;
  UUID owner;

  @BeforeEach
  void clean() {
    shops.deleteAll();
    owner = UUID.randomUUID();
    reset(approvals);
  }

  org.springframework.test.web.servlet.request.RequestPostProcessor as(UUID id, String role) {
    return jwt()
        .jwt(j -> j.subject(id.toString()))
        .authorities(new SimpleGrantedAuthority("ROLE_" + role));
  }

  Shop shop(ShopStatus status) {
    var s = new Shop();
    s.setName("Test Shop");
    s.setDescription("A neighbourhood test shop");
    s.setCategory("Grocery");
    s.setAddress("Main Street");
    s.setLatitude(10.0);
    s.setLongitude(76.0);
    s.setOwnerId(owner);
    s.setStatus(status);
    return shops.saveAndFlush(s);
  }

  @Test
  void ownerCannotApprovePendingShopThroughAvailabilityToggle() throws Exception {
    var s = shop(ShopStatus.PENDING);
    mvc.perform(put("/api/shops/" + s.getId() + "/toggle-status").with(as(owner, "SHOPKEEPER")))
        .andExpect(status().isConflict());
    assertThat(shops.findById(s.getId()).orElseThrow().getStatus()).isEqualTo(ShopStatus.PENDING);
  }

  @Test
  void explicitRejectionRemainsRejectedOnRetry() throws Exception {
    var s = shop(ShopStatus.PENDING);
    for (int i = 0; i < 2; i++)
      mvc.perform(
              put("/internal/shops/" + s.getId() + "/approval")
                  .header("X-Internal-Key", "test-only-internal-key-32-characters-long")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"status\":\"REJECTED\"}"))
          .andExpect(status().isOk());
    assertThat(shops.findById(s.getId()).orElseThrow().getStatus()).isEqualTo(ShopStatus.REJECTED);
  }

  @Test
  void internalApiDoesNotAcceptAUserToken() throws Exception {
    var s = shop(ShopStatus.PENDING);
    mvc.perform(
            put("/internal/shops/" + s.getId() + "/approval")
                .with(as(owner, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"APPROVED\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void otherShopkeepersCannotModifyProducts() throws Exception {
    var s = shop(ShopStatus.APPROVED);
    mvc.perform(
            post("/api/products")
                .param("shopId", s.getId().toString())
                .with(as(UUID.randomUUID(), "SHOPKEEPER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Milk\",\"available\":true}"))
        .andExpect(status().isForbidden());
    assertThat(products.count()).isZero();
  }

  @Test
  void productUpdatesApplyRequestedValuesAndAreRepeatable() throws Exception {
    var s = shop(ShopStatus.APPROVED);
    var p = new Product();
    p.setName("Old");
    p.setAvailable(false);
    p.setShop(s);
    p = products.saveAndFlush(p);
    for (int i = 0; i < 2; i++)
      mvc.perform(
              put("/api/products/" + p.getId())
                  .with(as(owner, "SHOPKEEPER"))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\":\"New\",\"available\":true}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("New"))
          .andExpect(jsonPath("$.available").value(true));
  }

  @Test
  void customersCannotSeePendingShops() throws Exception {
    var s = shop(ShopStatus.PENDING);
    mvc.perform(get("/api/shops").with(as(UUID.randomUUID(), "USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
    mvc.perform(get("/api/shops/" + s.getId()).with(as(UUID.randomUUID(), "USER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void shopUpdateCannotTransferOwnership() throws Exception {
    var s = shop(ShopStatus.APPROVED);
    mvc.perform(
            put("/api/shops/" + s.getId())
                .with(as(owner, "SHOPKEEPER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Shop\",\"description\":\"A long description\",\"category\":\"Grocery\",\"address\":\"Road\",\"latitude\":10,\"longitude\":76,\"ownerId\":\""
                        + UUID.randomUUID()
                        + "\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void realImagesCanBeUploadedDownloadedAndDeleted() throws Exception {
    var s = shop(ShopStatus.APPROVED);
    var bytes = new java.io.ByteArrayOutputStream();
    javax.imageio.ImageIO.write(
        new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB),
        "png",
        bytes);
    var file = new MockMultipartFile("file", "../../unsafe.png", "image/png", bytes.toByteArray());
    var response =
        mvc.perform(
                multipart("/api/images/upload")
                    .file(file)
                    .param("shopId", s.getId().toString())
                    .with(as(owner, "SHOPKEEPER")))
            .andExpect(status().isOk())
            .andReturn();
    String id =
        new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(response.getResponse().getContentAsString())
            .get("id")
            .asText();
    mvc.perform(get("/api/images/" + id + "/download").with(as(UUID.randomUUID(), "USER")))
        .andExpect(status().isOk())
        .andExpect(content().bytes(bytes.toByteArray()));
    mvc.perform(delete("/api/images/" + id).with(as(UUID.randomUUID(), "SHOPKEEPER")))
        .andExpect(status().isForbidden());
    mvc.perform(delete("/api/images/" + id).with(as(owner, "SHOPKEEPER")))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/images/" + id + "/download").with(as(owner, "SHOPKEEPER")))
        .andExpect(status().isNotFound());
  }

  @Test
  void fakeImagesAreRejectedBeforePersistence() throws Exception {
    var s = shop(ShopStatus.APPROVED);
    var file = new MockMultipartFile("file", "fake.png", "image/png", "not an image".getBytes());
    mvc.perform(
            multipart("/api/images/upload")
                .file(file)
                .param("shopId", s.getId().toString())
                .with(as(owner, "SHOPKEEPER")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void failedApprovalDeliveryRemainsRetryable() {
    var s = shop(ShopStatus.PENDING);
    doThrow(new RuntimeException("offline"))
        .doNothing()
        .when(approvals)
        .createApprovalRequest(s.getId(), 0);
    assertThatThrownBy(() -> delivery.deliver(s.getId())).isInstanceOf(RuntimeException.class);
    assertThat(shops.findById(s.getId()).orElseThrow().isApprovalRequested()).isFalse();
    delivery.deliver(s.getId());
    delivery.deliver(s.getId());
    assertThat(shops.findById(s.getId()).orElseThrow().isApprovalRequested()).isTrue();
    verify(approvals, times(2)).createApprovalRequest(s.getId(), 0);
  }
}

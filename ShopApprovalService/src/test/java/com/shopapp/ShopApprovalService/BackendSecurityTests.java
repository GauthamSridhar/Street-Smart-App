package com.shopapp.ShopApprovalService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.shopapp.ShopApprovalService.feign.ShopFeignClient;
import com.shopapp.ShopApprovalService.model.*;
import com.shopapp.ShopApprovalService.repository.ShopApprovalRepository;
import com.shopapp.ShopApprovalService.service.DecisionDelivery;
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

  @Autowired ShopApprovalRepository approvals;
  @Autowired DecisionDelivery delivery;
  @MockitoBean ShopFeignClient shops;

  @BeforeEach
  void clean() {
    approvals.deleteAll();
    reset(shops);
  }

  UUID pending() {
    var a = new ShopApproval();
    a.setShopId(UUID.randomUUID());
    a.setApprovalStatus(ShopStatus.PENDING);
    return approvals.saveAndFlush(a).getShopId();
  }

  org.springframework.test.web.servlet.request.RequestPostProcessor admin() {
    return jwt()
        .jwt(j -> j.subject(UUID.randomUUID().toString()))
        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
  }

  @Test
  void customerCannotApproveOrReadApprovalQueue() throws Exception {
    mvc.perform(post("/api/approvals/" + pending() + "/approve").with(jwt()))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/approvals/pending").with(jwt())).andExpect(status().isForbidden());
  }

  @Test
  void rejectRecordsActorReasonAndPendingDelivery() throws Exception {
    UUID id = pending();
    mvc.perform(
            post("/api/approvals/" + id + "/reject")
                .param("reason", "Address cannot be verified")
                .with(admin()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.approvalStatus").value("REJECTED"));
    var a = approvals.findByShopId(id).orElseThrow();
    assertThat(a.getDecidedBy()).isNotNull();
    assertThat(a.getReason()).isEqualTo("Address cannot be verified");
    assertThat(a.isSynchronizedWithShop()).isFalse();
    mvc.perform(post("/api/approvals/" + id + "/approve").with(admin()))
        .andExpect(status().isConflict());
  }

  @Test
  void retryingApprovalDoesNotCreateAnotherDecision() throws Exception {
    UUID id = pending();
    for (int i = 0; i < 2; i++)
      mvc.perform(post("/api/approvals/" + id + "/approve").with(admin()))
          .andExpect(status().isOk());
    assertThat(approvals.count()).isEqualTo(1);
  }

  @Test
  void failedDecisionDeliveryIsRetriedWithExactStatus() throws Exception {
    UUID id = pending();
    mvc.perform(
            post("/api/approvals/" + id + "/reject")
                .param("reason", "Invalid address")
                .with(admin()))
        .andExpect(status().isOk());
    doThrow(new RuntimeException("offline"))
        .doNothing()
        .when(shops)
        .applyDecision(id, Map.of("status", "REJECTED", "revision", "0"));
    assertThatThrownBy(() -> delivery.deliver(id)).isInstanceOf(RuntimeException.class);
    assertThat(approvals.findByShopId(id).orElseThrow().isSynchronizedWithShop()).isFalse();
    delivery.deliver(id);
    delivery.deliver(id);
    assertThat(approvals.findByShopId(id).orElseThrow().isSynchronizedWithShop()).isTrue();
    verify(shops, times(2)).applyDecision(id, Map.of("status", "REJECTED", "revision", "0"));
  }

  @Test
  void internalCreationIsIdempotentAndRequiresServiceCredential() throws Exception {
    UUID id = UUID.randomUUID();
    mvc.perform(post("/internal/approvals/" + id)).andExpect(status().isForbidden());
    for (int i = 0; i < 2; i++)
      mvc.perform(
              post("/internal/approvals/" + id)
                  .header("X-Internal-Key", "test-only-internal-key-32-characters-long"))
          .andExpect(status().isOk());
    assertThat(approvals.count()).isEqualTo(1);
  }
}

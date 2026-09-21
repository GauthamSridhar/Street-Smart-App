package com.shopapp.ShopApprovalService;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.shopapp.ShopApprovalService.model.*;
import com.shopapp.ShopApprovalService.repository.ShopApprovalRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResubmissionTests {
  @Autowired MockMvc mvc;
  @Autowired ShopApprovalRepository approvals;
  @Autowired JdbcTemplate jdbc;

  @Test
  void resubmissionPreservesHistoryAndIsIdempotent() throws Exception {
    var a = new ShopApproval();
    a.setShopId(UUID.randomUUID());
    a.setApprovalStatus(ShopStatus.REJECTED);
    a.setReason("Original reason");
    a = approvals.saveAndFlush(a);
    for (int i = 0; i < 2; i++)
      mvc.perform(
              post("/internal/approvals/" + a.getShopId())
                  .param("revision", "1")
                  .header("X-Internal-Key", "test-only-internal-key-32-characters-long"))
          .andExpect(status().isOk());
    assertThat(approvals.findByShopId(a.getShopId()).orElseThrow().getApprovalStatus())
        .isEqualTo(ShopStatus.PENDING);
    assertThat(
            jdbc.queryForObject(
                "select count(*) from approval_history where shop_id = ?",
                Long.class,
                a.getShopId()))
        .isEqualTo(1);
  }
}

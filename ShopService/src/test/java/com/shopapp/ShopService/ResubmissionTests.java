package com.shopapp.ShopService;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.shopapp.ShopService.model.*;
import com.shopapp.ShopService.repository.ShopRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResubmissionTests {
  @Autowired MockMvc mvc;
  @Autowired ShopRepository shops;

  @Test
  void staleDecisionCannotRejectNewApplication() throws Exception {
    UUID owner = UUID.randomUUID();
    var s = new Shop();
    s.setOwnerId(owner);
    s.setName("Retry shop");
    s.setDescription("Corrected details");
    s.setAddress("New address");
    s.setCategory("Grocery");
    s.setLatitude(10.0);
    s.setLongitude(76.0);
    s.setStatus(ShopStatus.REJECTED);
    s = shops.saveAndFlush(s);
    String path = "/api/shops/" + s.getId() + "/resubmit";
    mvc.perform(
            post(path)
                .with(
                    jwt()
                        .jwt(j -> j.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_SHOPKEEPER"))))
        .andExpect(status().isForbidden());
    mvc.perform(
            post(path)
                .with(
                    jwt()
                        .jwt(j -> j.subject(owner.toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_SHOPKEEPER"))))
        .andExpect(status().isOk());
    mvc.perform(
            put("/internal/shops/" + s.getId() + "/approval")
                .header("X-Internal-Key", "test-only-internal-key-32-characters-long")
                .contentType("application/json")
                .content("{\"status\":\"REJECTED\",\"revision\":0}"))
        .andExpect(status().isOk());
    assertThat(shops.findById(s.getId()).orElseThrow().getStatus()).isEqualTo(ShopStatus.PENDING);
    mvc.perform(
            put("/internal/shops/" + s.getId() + "/approval")
                .header("X-Internal-Key", "test-only-internal-key-32-characters-long")
                .contentType("application/json")
                .content("{\"status\":\"APPROVED\",\"revision\":1}"))
        .andExpect(status().isOk());
    assertThat(shops.findById(s.getId()).orElseThrow().getStatus()).isEqualTo(ShopStatus.APPROVED);
  }
}

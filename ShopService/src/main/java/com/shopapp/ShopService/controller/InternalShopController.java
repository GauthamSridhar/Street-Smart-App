package com.shopapp.ShopService.controller;

import com.shopapp.ShopService.model.ShopStatus;
import com.shopapp.ShopService.service.impl.ShopServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/shops")
@RequiredArgsConstructor
public class InternalShopController {
  private final ShopServiceImpl shops;

  public record Decision(@NotNull ShopStatus status, long revision) {}

  @PutMapping("/{id}/approval")
  public void decision(@PathVariable UUID id, @Valid @RequestBody Decision decision) {
    shops.applyDecision(id, decision.status(), decision.revision());
  }
}

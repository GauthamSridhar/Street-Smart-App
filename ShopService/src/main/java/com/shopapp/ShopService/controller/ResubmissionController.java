package com.shopapp.ShopService.controller;

import com.shopapp.ShopService.dto.shop.response.ShopResponse;
import com.shopapp.ShopService.service.impl.ShopServiceImpl;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ResubmissionController {
  private final ShopServiceImpl shops;

  @PostMapping("/api/shops/{id}/resubmit")
  public ShopResponse resubmit(@PathVariable UUID id) {
    return shops.resubmit(id);
  }
}

package com.shopapp.ShopApprovalService.feign;

import java.util.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ShopService", path = "/internal/shops")
public interface ShopFeignClient {
  @PutMapping("/{shopId}/approval")
  void applyDecision(@PathVariable UUID shopId, @RequestBody Map<String, String> decision);
}

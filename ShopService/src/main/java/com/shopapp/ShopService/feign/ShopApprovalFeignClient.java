package com.shopapp.ShopService.feign;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ShopApprovalService", path = "/internal/approvals")
public interface ShopApprovalFeignClient {
  @PostMapping("/{shopId}")
  void createApprovalRequest(@PathVariable UUID shopId, @RequestParam long revision);
}

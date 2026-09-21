package com.shopapp.ShopApprovalService.controller;

import com.shopapp.ShopApprovalService.service.impl.ShopApprovalServiceImpl;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/approvals")
@RequiredArgsConstructor
public class InternalApprovalController {
  private final ShopApprovalServiceImpl approvals;

  @PostMapping("/{shopId}")
  public void create(@PathVariable UUID shopId, @RequestParam(defaultValue = "0") long revision) {
    approvals.createApprovalRequest(shopId, revision);
  }
}

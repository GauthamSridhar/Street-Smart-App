package com.shopapp.ShopApprovalService.service;

import com.shopapp.ShopApprovalService.feign.ShopFeignClient;
import com.shopapp.ShopApprovalService.repository.ShopApprovalRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DecisionDelivery {
  private final ShopApprovalRepository approvals;
  private final ShopFeignClient shops;

  @Transactional
  public void deliver(UUID shopId) {
    var approval = approvals.locked(shopId).orElseThrow();
    if (approval.isSynchronizedWithShop()) return;
    shops.applyDecision(
        shopId,
        Map.of(
            "status",
            approval.getApprovalStatus().name(),
            "revision",
            Long.toString(approval.getRevision())));
    approval.setSynchronizedWithShop(true);
  }
}

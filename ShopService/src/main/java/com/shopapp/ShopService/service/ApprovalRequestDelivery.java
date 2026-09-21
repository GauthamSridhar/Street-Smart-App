package com.shopapp.ShopService.service;

import com.shopapp.ShopService.feign.ShopApprovalFeignClient;
import com.shopapp.ShopService.repository.ShopRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprovalRequestDelivery {
  private final ShopRepository shops;
  private final ShopApprovalFeignClient approvals;

  @Transactional
  public void deliver(UUID id) {
    var shop = shops.locked(id).orElseThrow();
    if (shop.isApprovalRequested()) return;
    approvals.createApprovalRequest(id, shop.getApprovalRevision());
    shop.setApprovalRequested(true);
  }
}

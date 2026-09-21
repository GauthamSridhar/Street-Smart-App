package com.shopapp.ShopService.service;

import com.shopapp.ShopService.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "delivery.enabled", havingValue = "true", matchIfMissing = true)
public class ApprovalRequestWorker {
  private final ShopRepository shops;
  private final ApprovalRequestDelivery delivery;

  @Scheduled(fixedDelayString = "${delivery.interval-ms:5000}")
  public void deliverPending() {
    for (var shop : shops.findTop50ByApprovalRequestedFalseOrderById()) {
      try {
        delivery.deliver(shop.getId());
      } catch (RuntimeException ex) {
        log.warn(
            "Approval request {} remains pending ({})",
            shop.getId(),
            ex.getClass().getSimpleName());
      }
    }
  }
}

package com.shopapp.ShopApprovalService.service;

import com.shopapp.ShopApprovalService.repository.ShopApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "delivery.enabled", havingValue = "true", matchIfMissing = true)
public class DecisionWorker {
  private final ShopApprovalRepository approvals;
  private final DecisionDelivery delivery;

  @Scheduled(fixedDelayString = "${delivery.interval-ms:5000}")
  public void deliverPending() {
    for (var approval : approvals.findTop50BySynchronizedWithShopFalseOrderById()) {
      try {
        delivery.deliver(approval.getShopId());
      } catch (RuntimeException ex) {
        log.warn(
            "Decision for shop {} remains pending ({})",
            approval.getShopId(),
            ex.getClass().getSimpleName());
      }
    }
  }
}

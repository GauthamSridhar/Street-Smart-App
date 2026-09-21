package com.shopapp.ShopApprovalService.service;

import com.shopapp.ShopApprovalService.repository.ShopApprovalRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class DeliveryMetrics {
  public DeliveryMetrics(MeterRegistry registry, ShopApprovalRepository approvals) {
    registry.gauge(
        "street.smart.approval.decisions.pending",
        approvals,
        ShopApprovalRepository::countBySynchronizedWithShopFalse);
  }
}

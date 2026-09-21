package com.shopapp.ShopService.service;

import com.shopapp.ShopService.repository.ShopRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class DeliveryMetrics {
  public DeliveryMetrics(MeterRegistry registry, ShopRepository shops) {
    registry.gauge(
        "street.smart.approval.requests.pending",
        shops,
        ShopRepository::countByApprovalRequestedFalse);
  }
}

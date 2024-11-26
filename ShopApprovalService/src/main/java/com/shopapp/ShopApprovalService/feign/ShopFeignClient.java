package com.shopapp.ShopApprovalService.feign;

import com.shopapp.ShopApprovalService.model.ShopStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "ShopService", path = "/api/shops")
public interface ShopFeignClient {

    @PutMapping("/{shopId}/status")
    void updateShopStatus(
            @PathVariable("shopId") UUID shopId,
            @RequestParam("status") ShopStatus status);
}

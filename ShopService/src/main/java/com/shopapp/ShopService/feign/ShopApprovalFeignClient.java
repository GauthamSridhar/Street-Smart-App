package com.shopapp.ShopService.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "ShopApprovalService", path = "/api/approvals")
public interface ShopApprovalFeignClient {

    @PostMapping("/create")
    void createApprovalRequest(@RequestParam("shopId") UUID shopId);
}

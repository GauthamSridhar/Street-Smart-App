package com.shopapp.RatingService.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "ShopService", path = "/api/shops")
public interface ShopFeignClient {

    @GetMapping("/{shopId}/exists")
    Boolean doesShopExist(@PathVariable("shopId") UUID shopId);
}

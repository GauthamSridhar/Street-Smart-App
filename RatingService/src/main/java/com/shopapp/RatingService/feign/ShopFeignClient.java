package com.shopapp.RatingService.feign;

import com.shopapp.RatingService.dto.rating.ShopResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ShopService", path = "/api/shops")
public interface ShopFeignClient {
  @GetMapping("/{shopId}")
  ShopResponse getShop(@PathVariable UUID shopId);
}

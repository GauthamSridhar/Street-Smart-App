package com.shopapp.RatingService.feign;

import com.shopapp.RatingService.dto.rating.ShopResponse;
import com.shopapp.RatingService.dto.rating.UpdateShopRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "ShopService", path = "/api/shops")
public interface ShopFeignClient {

    @GetMapping("/{shopId}")
    ResponseEntity<ShopResponse> getShop(@PathVariable("shopId") UUID shopId, @RequestHeader("Authorization") String authToken);

    @PutMapping("/{shopId}")
    void updateShop(
            @PathVariable UUID shopId,
            @RequestBody UpdateShopRequest request);
}

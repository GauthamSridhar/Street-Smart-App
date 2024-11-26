package com.shopapp.FavoriteService.feign;

import com.shopapp.FavoriteService.dto.favourite.ShopBasicInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "ShopService", path = "/api/shops")
public interface ShopFeignClient {

    @GetMapping("/{shopId}/exists")
    Boolean doesShopExist(@PathVariable("shopId") UUID shopId);

    @GetMapping("/{shopId}/basic-info")
    ShopBasicInfoDTO getShopBasicInfo(@PathVariable("shopId") UUID shopId);

}


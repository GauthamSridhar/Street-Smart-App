package com.shopapp.ShopService.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "UserService", path = "/api/users")
public interface UserServiceFeignClient {

    @GetMapping("/{userId}/exists")
    ResponseEntity<Boolean> doesUserExist(@PathVariable("userId") UUID userId);
}

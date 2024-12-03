package com.shopapp.ShopService.feign;

import com.shopapp.ShopService.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "UserService", path = "/api/users")
public interface UserServiceFeignClient {


    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId, @RequestHeader("Authorization") String authToken);
}

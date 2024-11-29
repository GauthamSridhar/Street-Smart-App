package com.shopapp.RatingService.feign;

import com.shopapp.RatingService.dto.rating.UpdateUserRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "UserService", path = "/api/users")
public interface UserFeignClient {

    @GetMapping("/{userId}")
    UpdateUserRequest getUserById(@PathVariable("userId") UUID userId);

    @PutMapping("/{userId}")
    void updateProfile(@PathVariable("userId") UUID userId, @RequestBody UpdateUserRequest userDTO);
}

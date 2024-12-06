package com.shopapp.RatingService.feign;

import com.shopapp.RatingService.dto.rating.UpdateUserRequest;
import com.shopapp.RatingService.dto.rating.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "UserService", path = "/api/users")
public interface UserFeignClient {

    @GetMapping("/{userId}")
    ResponseEntity<UserResponse> getUser(@PathVariable("userId") UUID userId, @RequestHeader("Authorization") String authToken);

    @PutMapping("/{userId}")
    void updateProfile(@PathVariable("userId") UUID userId, @RequestBody UpdateUserRequest userDTO, @RequestHeader("Authorization") String authToken);
}

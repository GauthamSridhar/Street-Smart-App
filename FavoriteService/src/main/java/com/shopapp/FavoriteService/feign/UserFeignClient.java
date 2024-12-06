package com.shopapp.FavoriteService.feign;

import com.shopapp.FavoriteService.dto.favourite.UpdateUserRequest;
import com.shopapp.FavoriteService.dto.favourite.UserDTO;
import com.shopapp.FavoriteService.dto.favourite.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "UserService", path = "/api/users")
public interface UserFeignClient {

    @GetMapping("/{userId}")
    ResponseEntity<UserResponse> getUser(@PathVariable("userId") UUID userId, @RequestHeader("Authorization") String authToken);

    @PutMapping("/{userId}")
    void updateProfile(@PathVariable("userId") UUID userId, @RequestBody UpdateUserRequest userDTO,@RequestHeader("Authorization") String authToken);
}

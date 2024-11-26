package com.shopapp.FavoriteService.feign;

import com.shopapp.FavoriteService.dto.favourite.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "UserService", path = "/api/users")
public interface UserFeignClient {

    @GetMapping("/{userId}")
    UserDTO getUserById(@PathVariable("userId") UUID userId);

    @PutMapping("/{userId}")
    void updateUser(@PathVariable("userId") UUID userId, @RequestBody UserDTO userDTO);
}

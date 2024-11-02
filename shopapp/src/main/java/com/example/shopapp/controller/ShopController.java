package com.example.shopapp.controller;

import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopStatus;
import com.example.shopapp.model.User;
import com.example.shopapp.service.ShopService;
import com.example.shopapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Shop> registerShop(
            @RequestParam UUID userId,
            @Valid @RequestBody Shop shop) {
        User owner = userService.findUserById(userId); // Assuming a method to fetch user by ID
        shop.setOwner(owner);
        Shop registeredShop = shopService.registerShop(owner, shop);
        return ResponseEntity.ok(registeredShop);
    }
    @GetMapping("/{shopId}")
    public ResponseEntity<Shop> getShop(@PathVariable UUID shopId) {
        Shop shop = shopService.getShopById(shopId);
        return ResponseEntity.ok(shop);
    }

    @PutMapping("/{shopId}/status")
    public ResponseEntity<Shop> updateShopStatus(
            @PathVariable UUID shopId,
            @RequestParam ShopStatus status) {
        Shop updatedShop = shopService.updateShopStatus(shopId, status);
        return ResponseEntity.ok(updatedShop);
    }

    @GetMapping("/{shopId}/analytics")
    public ResponseEntity<?> viewShopAnalytics(@PathVariable UUID shopId) {
        return ResponseEntity.ok(shopService.viewShopAnalytics(shopId));
    }
}

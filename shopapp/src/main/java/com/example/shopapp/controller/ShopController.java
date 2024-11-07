package com.example.shopapp.controller;

import com.example.shopapp.dto.request.ShopRegistrationRequest;
import com.example.shopapp.dto.response.ShopDetailResponse;
import com.example.shopapp.dto.response.ShopResponse;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopStatus;
import com.example.shopapp.service.ShopService;
import com.example.shopapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ShopResponse> registerShop(
            @RequestParam UUID userId,
            @Valid @RequestBody ShopRegistrationRequest shop) {
        UUID owner = userService.findUserById(userId).getId();
        if (owner == null) {
            return ResponseEntity.notFound().build();
        }

        ShopResponse registeredShop = shopService.registerShop(owner, shop);
        return ResponseEntity.ok(registeredShop);
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<ShopDetailResponse> getShop(@PathVariable UUID shopId) {
        ShopDetailResponse shop = shopService.getShopById(shopId);
        return ResponseEntity.ok(shop);
    }

    @PutMapping("/{shopId}/status")
    public ResponseEntity<ShopResponse> updateShopStatus(
            @PathVariable UUID shopId,
            @RequestParam ShopStatus status) {
        ShopResponse updatedShop = shopService.updateShopStatus(shopId, status);
        return ResponseEntity.ok(updatedShop);
    }
}

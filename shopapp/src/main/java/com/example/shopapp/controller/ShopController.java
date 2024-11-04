package com.example.shopapp.controller;

import com.example.shopapp.model.Product;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopStatus;
import com.example.shopapp.model.User;
import com.example.shopapp.service.ShopService;
import com.example.shopapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Shop> registerShop(
            @RequestParam UUID userId,
            @Valid @RequestBody Shop shop) {
        User owner = userService.findUserById(userId);
        if (owner == null) {
            return ResponseEntity.notFound().build();
        }
        shop.setOwner(owner);
        Shop registeredShop = shopService.registerShop(owner, shop);
        return ResponseEntity.ok(registeredShop);
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<Shop> getShop(@PathVariable UUID shopId) {
        Shop shop = shopService.getShopById(shopId);
        if (shop == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(shop);
    }

    @PutMapping("/{shopId}/status")
    public ResponseEntity<Shop> updateShopStatus(
            @PathVariable UUID shopId,
            @RequestParam ShopStatus status) {
        Shop updatedShop = shopService.updateShopStatus(shopId, status);
        if (updatedShop == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedShop);
    }

    @PostMapping("/{shopId}/products")
    public ResponseEntity<Shop> addProductToInventory(
            @PathVariable UUID shopId,
            @Valid @RequestBody Product product) {
        Shop updatedShop = shopService.addProductToInventory(shopId, product);
        if (updatedShop == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedShop);
    }

    @PostMapping("/{shopId}/images")
    public ResponseEntity<Shop> uploadShopImages(
            @PathVariable UUID shopId,
            @RequestBody List<String> imageUrls) {
        Shop updatedShop = shopService.uploadShopImages(shopId, imageUrls);
        if (updatedShop == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedShop);
    }
}

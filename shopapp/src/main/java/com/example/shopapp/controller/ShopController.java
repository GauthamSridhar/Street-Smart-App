package com.example.shopapp.controller;

import com.example.shopapp.dto.request.ShopRegistrationRequest;
import com.example.shopapp.dto.response.ShopDetailResponse;
import com.example.shopapp.dto.response.ShopResponse;
import com.example.shopapp.model.ShopStatus;
import com.example.shopapp.service.ShopService;
import com.example.shopapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for shop-related operations.
 */
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final UserService userService;

    /**
     * Registers a new shop.
     *
     * @param userId the ID of the user registering the shop
     * @param request the shop registration request DTO
     * @return the registered shop response DTO
     */
    @PostMapping("/register")
    public ResponseEntity<ShopResponse> registerShop(
            @RequestParam UUID userId,
            @Valid @RequestBody ShopRegistrationRequest request) {
        ShopResponse registeredShop = shopService.registerShop(userId, request);
        return ResponseEntity.ok(registeredShop);
    }

    /**
     * Retrieves shop details by ID.
     *
     * @param shopId the shop ID
     * @return the shop detail response DTO
     */
    @GetMapping("/{shopId}")
    public ResponseEntity<ShopDetailResponse> getShop(@PathVariable UUID shopId) {
        ShopDetailResponse shop = shopService.getShopById(shopId);
        return ResponseEntity.ok(shop);
    }

    /**
     * Updates the status of a shop.
     *
     * @param shopId the shop ID
     * @param status the new shop status
     * @return the updated shop response DTO
     */
    @PutMapping("/{shopId}/status")
    public ResponseEntity<ShopResponse> updateShopStatus(
            @PathVariable UUID shopId,
            @RequestParam ShopStatus status) {
        ShopResponse updatedShop = shopService.updateShopStatus(shopId, status);
        return ResponseEntity.ok(updatedShop);
    }

}

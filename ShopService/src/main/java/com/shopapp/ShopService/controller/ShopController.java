package com.shopapp.ShopService.controller;

import com.shopapp.ShopService.dto.ShopBasicInfoDTO;
import com.shopapp.ShopService.dto.UpdateShopRequest;
import com.shopapp.ShopService.dto.shop.request.ShopRegistrationRequest;
import com.shopapp.ShopService.dto.shop.response.ShopResponse;
import com.shopapp.ShopService.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for shop-related operations.
 */
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    /**
     * Registers a new shop.
     *
     * @param userId  the ID of the user registering the shop
     * @param request the shop registration request DTO
     * @return the registered shop response DTO
     */
    @PostMapping("/register")
    public ResponseEntity<ShopResponse> registerShop(
            @RequestParam("userId") UUID userId,
            @Valid @RequestBody ShopRegistrationRequest request, HttpServletRequest req) {
        System.out.println("ShopController.registerShop");
        ShopResponse registeredShop = shopService.registerShop(userId, request, req);
        return ResponseEntity.ok(registeredShop);
    }

    /**
     * Retrieves shop details by ID.
     *
     * @param shopId the shop ID
     * @return the shop detail response DTO
     */
    @GetMapping("/{shopId}")
    public ResponseEntity<ShopResponse> getShop(@PathVariable UUID shopId) {
        ShopResponse shop = shopService.getShopById(shopId);
        return ResponseEntity.ok(shop);
    }

    /**
     * Updates the status of a shop.
     *
     * @param shopId the shop ID
     * @param request the new shop status
     * @return the updated shop response DTO
     */
    @PutMapping("/{shopId}")
    public ResponseEntity<ShopResponse> updateShop(
            @PathVariable UUID shopId,
            @RequestBody UpdateShopRequest request) {
        ShopResponse updatedShop = shopService.updateShop(shopId, request);
        return ResponseEntity.ok(updatedShop);
    }

    /**
     * Toggles the status of a shop between ACTIVE and INACTIVE.
     *
     * @param shopId the shop ID
     * @return the updated shop response DTO
     */
    @PutMapping("/{shopId}/toggle-status")
    public ResponseEntity<ShopResponse> toggleShopStatus(@PathVariable UUID shopId) {
        ShopResponse updatedShop = shopService.toggleShopStatus(shopId);
        return ResponseEntity.ok(updatedShop);
    }
    @GetMapping("/{shopId}/exists")
    public ResponseEntity<Boolean> doesShopExist(@PathVariable UUID shopId) {
        boolean exists = shopService.doesShopExist(shopId);
        return ResponseEntity.ok(exists);
    }
    @GetMapping("/{shopId}/basic-info")
    public ResponseEntity<ShopBasicInfoDTO> getShopBasicInfo(@PathVariable UUID shopId) {
        ShopBasicInfoDTO shopInfo = shopService.getShopBasicInfo(shopId);
        return ResponseEntity.ok(shopInfo);
    }
    @GetMapping("/owner/{userId}")
    public ResponseEntity<ShopResponse> getShopByOwner(@PathVariable UUID userId) {
        ShopResponse shop = shopService.getShopByOwner(userId);
        return ResponseEntity.ok(shop);
    }
    //get all shops data by a list
    @GetMapping
    public ResponseEntity<List<ShopResponse>> getAllShops() {
        List<ShopResponse> shops = shopService.getAllShops();
        return ResponseEntity.ok(shops);
    }


}

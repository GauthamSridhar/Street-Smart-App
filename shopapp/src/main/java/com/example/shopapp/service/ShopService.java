package com.example.shopapp.service;

import com.example.shopapp.dto.request.AddProductRequest;
import com.example.shopapp.dto.request.ShopRegistrationRequest;
import com.example.shopapp.dto.response.ShopDetailResponse;
import com.example.shopapp.dto.response.ShopResponse;
import com.example.shopapp.dto.response.ShopSummaryResponse;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopStatus;
import jakarta.validation.Valid;

import java.util.UUID;
import java.util.List;

public interface ShopService {
    ShopResponse registerShop(UUID ownerId, @Valid ShopRegistrationRequest request);
    ShopResponse updateShopStatus(UUID shopId, ShopStatus request);
    ShopDetailResponse getShopById(UUID shopId);
    List<ShopSummaryResponse> getAllShops();
    ShopResponse addProductToInventory(UUID shopId, AddProductRequest request);
}
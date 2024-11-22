package com.example.shopapp.service;

import com.example.shopapp.dto.request.AddProductRequest;
import com.example.shopapp.dto.request.ShopRegistrationRequest;
import com.example.shopapp.dto.response.ShopDetailResponse;
import com.example.shopapp.dto.response.ShopResponse;
import com.example.shopapp.model.ShopStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ShopService {


    ShopResponse registerShop(UUID ownerId, @Valid ShopRegistrationRequest request);
    ShopResponse updateShopStatus(UUID shopId, ShopStatus status);
    ShopDetailResponse getShopById(UUID shopId);
}

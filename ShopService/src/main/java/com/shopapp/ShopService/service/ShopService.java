package com.shopapp.ShopService.service;

import com.shopapp.ShopService.dto.ShopBasicInfoDTO;
import com.shopapp.ShopService.dto.UpdateShopRequest;
import com.shopapp.ShopService.dto.shop.request.ShopRegistrationRequest;
import com.shopapp.ShopService.dto.shop.response.ShopResponse;
import com.shopapp.ShopService.model.ShopStatus;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface ShopService {

    ShopResponse registerShop(UUID ownerId, ShopRegistrationRequest request, HttpServletRequest req);

    ShopResponse getShopById(UUID shopId);

    ShopResponse updateShop(UUID shopId, UpdateShopRequest request);

    ShopResponse toggleShopStatus(UUID shopId);

    boolean doesShopExist(UUID shopId);

    ShopBasicInfoDTO getShopBasicInfo(UUID shopId);

    ShopResponse getShopByOwner(UUID userId);

    List<ShopResponse> getAllShops();
}

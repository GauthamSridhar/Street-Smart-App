package com.shopapp.ShopService.service;

import com.shopapp.ShopService.dto.ShopBasicInfoDTO;
import com.shopapp.ShopService.dto.shop.request.ShopRegistrationRequest;
import com.shopapp.ShopService.dto.shop.response.ShopResponse;
import com.shopapp.ShopService.model.ShopStatus;

import java.util.UUID;

public interface ShopService {

    ShopResponse registerShop(UUID ownerId, ShopRegistrationRequest request);

    ShopResponse getShopById(UUID shopId);

    ShopResponse updateShopStatus(UUID shopId, ShopStatus status);

    ShopResponse toggleShopStatus(UUID shopId);

    boolean doesShopExist(UUID shopId);

    ShopBasicInfoDTO getShopBasicInfo(UUID shopId);
}

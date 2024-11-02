// service/ShopService.java
package com.example.shopapp.service;

import com.example.shopapp.model.Product;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopStatus;
import com.example.shopapp.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface ShopService {
    @Transactional
    Shop registerShop(User owner, Shop request);
    Shop registerShop(UUID ownerId, Shop request);
    Shop addProductToInventory(UUID shopId, Product product);
    Shop updateShopStatus(UUID shopId, ShopStatus status);
    Shop uploadShopImages(UUID shopId, List<byte[]> images);
    Object viewShopAnalytics(UUID shopId);

    Shop getShopById(UUID shopId);
}

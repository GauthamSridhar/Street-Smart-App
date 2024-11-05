package com.example.shopapp.service;

import com.example.shopapp.model.Product;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopStatus;
import com.example.shopapp.model.User;

import java.util.List;
import java.util.UUID;

public interface ShopService {
    Shop registerShop(User owner, Shop shop); // Update method to include adminId
    Shop getShopById(UUID shopId);
    Shop updateShopStatus(UUID shopId, ShopStatus status);
    Shop addProductToInventory(UUID shopId, Product product);
    Shop uploadShopImages(UUID shopId, List<String> imageUrls);
}

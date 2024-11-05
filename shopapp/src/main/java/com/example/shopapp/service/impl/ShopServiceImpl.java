package com.example.shopapp.service.impl;

import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.model.Product;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopStatus;
import com.example.shopapp.model.User;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.service.ShopApprovalService;
import com.example.shopapp.service.ShopService;
import com.example.shopapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopApprovalService shopApprovalService; // Injecting ShopApprovalService
    private final UserService userService; // Injecting UserService
    private final ShopRepository shopRepository; // Injecting ShopRepository for actual data persistence

    @Transactional
    @Override
    public Shop registerShop(User owner, Shop request, UUID adminId) { // Accept adminId
        request.setOwner(owner);
        request.setStatus(ShopStatus.PENDING); // Default status when registered

        // Save the shop
        Shop registeredShop = shopRepository.save(request); // Save shop to the database

        // Create a ShopApproval entry using the ShopApprovalService
        shopApprovalService.createApprovalRequest(registeredShop, adminId); // Pass adminId to the service

        return registeredShop;
    }

    @Transactional
    @Override
    public Shop addProductToInventory(UUID shopId, Product product) {
        Shop shop = getShopById(shopId); // Retrieve shop using service
        product.setShop(shop);  // Set the shop reference in the Product entity
        shop.getProducts().add(product);  // Add product to shop's inventory
        return shopRepository.save(shop); // Save updated shop using repository
    }

    @Transactional
    @Override
    public Shop updateShopStatus(UUID shopId, ShopStatus status) {
        Shop shop = getShopById(shopId); // Retrieve shop using service
        shop.setStatus(status);
        return shopRepository.save(shop); // Save updated shop using repository
    }

    @Transactional
    @Override
    public Shop uploadShopImages(UUID shopId, List<String> imageUrls) {
        Shop shop = getShopById(shopId); // Retrieve shop using service
        shop.getImages().addAll(imageUrls);  // Add image URLs to shop's image collection
        return shopRepository.save(shop); // Save updated shop using repository
    }

    @Override
    public Shop getShopById(UUID shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop with ID " + shopId + " not found"));
    }

    // You may want to add this method for saving a shop, if needed elsewhere
    @Transactional
    public Shop saveShop(Shop shop) {
        return shopRepository.save(shop);
    }
}

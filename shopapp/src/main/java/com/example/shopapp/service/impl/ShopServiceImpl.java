package com.example.shopapp.service.impl;

import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.model.Product;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopStatus;
import com.example.shopapp.model.User;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.repository.UserRepository;
import com.example.shopapp.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public Shop registerShop(User owner, Shop request) {
        request.setOwner(owner);
        request.setStatus(ShopStatus.PENDING);
        return shopRepository.save(request);
    }

    @Transactional
    @Override
    public Shop registerShop(UUID ownerId, Shop request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + ownerId + " not found"));
        request.setOwner(owner);
        request.setStatus(ShopStatus.PENDING);
        return shopRepository.save(request);
    }

    @Transactional
    @Override
    public Shop addProductToInventory(UUID shopId, Product product) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop with ID " + shopId + " not found"));
        product.setShop(shop);  // Set the shop reference in the Product entity
        shop.getProducts().add(product);  // Add product to shop's inventory
        return shopRepository.save(shop);
    }

    @Transactional
    @Override
    public Shop updateShopStatus(UUID shopId, ShopStatus status) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop with ID " + shopId + " not found"));
        shop.setStatus(status);
        return shopRepository.save(shop);
    }

    @Transactional
    @Override
    public Shop uploadShopImages(UUID shopId, List<String> imageUrls) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop with ID " + shopId + " not found"));

        shop.getImages().addAll(imageUrls);  // Add image URLs to shop's image collection
        return shopRepository.save(shop);
    }

    @Override
    public Object viewShopAnalytics(UUID shopId) {
        // Placeholder: Actual analytics logic can be implemented here
        return new Object();  // Replace with actual analytics data object
    }

    @Override
    public Shop getShopById(UUID shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop with ID " + shopId + " not found"));
    }
}

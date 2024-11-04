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

    private Shop getShopOrThrow(UUID shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop with ID " + shopId + " not found"));
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));
    }

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
        User owner = getUserOrThrow(ownerId);
        return registerShop(owner, request);
    }

    @Transactional
    @Override
    public Shop addProductToInventory(UUID shopId, Product product) {
        Shop shop = getShopOrThrow(shopId);
        product.setShop(shop);
        shop.getProducts().add(product);
        return shopRepository.save(shop);
    }

    @Transactional
    @Override
    public Shop updateShopStatus(UUID shopId, ShopStatus status) {
        Shop shop = getShopOrThrow(shopId);
        shop.setStatus(status);
        return shopRepository.save(shop);
    }

    @Transactional
    @Override
    public Shop uploadShopImages(UUID shopId, List<String> imageUrls) {
        Shop shop = getShopOrThrow(shopId);
        shop.getImages().addAll(imageUrls);
        return shopRepository.save(shop);
    }


    @Override
    public Shop getShopById(UUID shopId) {
        return getShopOrThrow(shopId);
    }
}

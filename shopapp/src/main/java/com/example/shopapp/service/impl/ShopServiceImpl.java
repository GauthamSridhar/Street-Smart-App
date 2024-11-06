package com.example.shopapp.service.impl;

import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.model.*;
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

    private final ShopApprovalService shopApprovalService;
    private final UserService userService;
    private final ShopRepository shopRepository;

    @Transactional
    @Override
    public Shop registerShop(User owner, Shop request) {
        request.setOwner(owner);
        request.setStatus(ShopStatus.PENDING);
        Shop registeredShop = shopRepository.save(request);
        shopApprovalService.createApprovalRequest(registeredShop);
        return registeredShop;
    }

    @Transactional
    @Override
    public Shop addProductToInventory(UUID shopId, Product product) {
        Shop shop = getShopById(shopId);
        product.setShop(shop);
        shop.getProducts().add(product);
        return shopRepository.save(shop);
    }

    @Transactional
    @Override
    public Shop updateShopStatus(UUID shopId, ShopStatus status) {
        Shop shop = getShopById(shopId);
        shop.setStatus(status);
        return shopRepository.save(shop);
    }

    @Override
    public Shop getShopById(UUID shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop with ID " + shopId + " not found"));
    }
}

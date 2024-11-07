package com.example.shopapp.service.impl;

import com.example.shopapp.dto.request.AddProductRequest;
import com.example.shopapp.dto.request.ShopRegistrationRequest;
import com.example.shopapp.dto.response.ShopDetailResponse;
import com.example.shopapp.dto.response.ShopResponse;
import com.example.shopapp.dto.response.ShopSummaryResponse;
import com.example.shopapp.dto.response.UserResponse;
import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.mapper.ShopMapper;
import com.example.shopapp.mapper.ProductMapper; // Import ProductMapper
import com.example.shopapp.model.Product;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopStatus;
import com.example.shopapp.model.User;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.service.ShopApprovalService;
import com.example.shopapp.service.ShopService;
import com.example.shopapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {
    private final ShopApprovalService shopApprovalService;
    private final UserService userService;
    private final ShopRepository shopRepository;
    private final ShopMapper shopMapper;
    private final ProductMapper productMapper; // Inject ProductMapper

    @Transactional
    @Override
    public ShopResponse registerShop(UUID ownerId, @Valid ShopRegistrationRequest request) {
        // Fetch the full User entity (not UserResponse) here
        User owner = userService.findUserById(ownerId); // Now returns User entity

        Shop shop = shopMapper.toEntity(request);
        shop.setOwner(owner); // Set the User entity as the owner

        Shop registeredShop = shopRepository.save(shop);
        shopApprovalService.createApprovalRequest(registeredShop);

        return shopMapper.toResponse(registeredShop);
    }

    @Transactional
    @Override
    public ShopResponse addProductToInventory(UUID shopId, AddProductRequest request) {
        Shop shop = findShopById(shopId);

        if (shop.getStatus() != ShopStatus.APPROVED) {
            throw new IllegalStateException("Shop must be approved to add products");
        }

        Product product = productMapper.toEntity(request); // Use ProductMapper to map request to Product
        product.setShop(shop);
        shop.getProducts().add(product);

        Shop updatedShop = shopRepository.save(shop);
        return shopMapper.toResponse(updatedShop);
    }

    @Transactional
    @Override
    public ShopResponse updateShopStatus(UUID shopId, ShopStatus request) {
        Shop shop = findShopById(shopId);
        shop.setStatus(request);
        Shop updatedShop = shopRepository.save(shop);
        return shopMapper.toResponse(updatedShop);
    }

    @Override
    public ShopDetailResponse getShopById(UUID shopId) {
        Shop shop = findShopById(shopId);
        return shopMapper.toDetailResponse(shop);
    }

    @Override
    public List<ShopSummaryResponse> getAllShops() {
        return shopRepository.findAll().stream()
                .map(shopMapper::toSummaryResponse)
                .toList();
    }

    private Shop findShopById(UUID shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
    }
}

package com.example.shopapp.service.impl;

import com.example.shopapp.dto.request.ShopRegistrationRequest;
import com.example.shopapp.dto.response.ShopDetailResponse;
import com.example.shopapp.dto.response.ShopResponse;
import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.mapper.ShopMapper;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopStatus;
import com.example.shopapp.model.User;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.service.ShopApprovalService;
import com.example.shopapp.service.ShopService;
import com.example.shopapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserService userService;
    private final ShopApprovalService shopApprovalService;
    private final ShopMapper shopMapper;

    @Override
    @Transactional
    public ShopResponse registerShop(UUID ownerId, ShopRegistrationRequest request) {
        log.info("Registering shop for user ID: {}", ownerId);

        User owner = userService.findUserById(ownerId);
        Shop shop = shopMapper.toEntity(request);
        shop.setOwner(owner);
        shop.setStatus(ShopStatus.PENDING);

        Shop savedShop = shopRepository.save(shop);
        shopApprovalService.createApprovalRequest(savedShop);

        log.info("Shop registered successfully with ID: {}", savedShop.getId());
        return shopMapper.toResponse(savedShop);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopDetailResponse getShopById(UUID shopId) {
        log.info("Fetching details for shop ID: {}", shopId);
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with ID: " + shopId));
        return shopMapper.toDetailResponse(shop);
    }




    @Override
    @Transactional
    public ShopResponse updateShopStatus(UUID shopId, ShopStatus status) {
        log.info("Updating status for shop ID: {} to {}", shopId, status);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with ID: " + shopId));

        shop.setStatus(status);
        Shop updatedShop = shopRepository.save(shop);

        log.info("Shop status updated successfully for ID: {}", updatedShop.getId());
        return shopMapper.toResponse(updatedShop);
    }
}

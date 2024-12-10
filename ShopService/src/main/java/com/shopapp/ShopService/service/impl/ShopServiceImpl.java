package com.shopapp.ShopService.service.impl;

import com.shopapp.ShopService.dto.ShopBasicInfoDTO;
import com.shopapp.ShopService.dto.UpdateShopRequest;
import com.shopapp.ShopService.dto.UserResponse;
import com.shopapp.ShopService.dto.shop.request.ShopRegistrationRequest;
import com.shopapp.ShopService.dto.shop.response.ShopResponse;
import com.shopapp.ShopService.exception.ResourceNotFoundException;
import com.shopapp.ShopService.feign.ShopApprovalFeignClient;
import com.shopapp.ShopService.feign.UserServiceFeignClient;
import com.shopapp.ShopService.mapper.ShopMapper;
import com.shopapp.ShopService.model.Shop;
import com.shopapp.ShopService.model.ShopStatus;
import com.shopapp.ShopService.repository.ShopRepository;
import com.shopapp.ShopService.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserServiceFeignClient userServiceFeignClient;
    private final ShopApprovalFeignClient shopApprovalFeignClient;
    private final ShopMapper shopMapper;

    @Override
    @Transactional
    public ShopResponse registerShop(UUID ownerId, ShopRegistrationRequest request, HttpServletRequest req) {

        String bearerToken = req.getHeader("Authorization");

        if (bearerToken == null) {
            return null;
        }

        log.info("Registering shop for owner ID: {}", ownerId);

        // Validate user existence via UserService
        UserResponse userExists = userServiceFeignClient.getUser(ownerId, bearerToken).getBody();
        if (userExists == null || userExists.getId() == null) {
            log.warn("User not found with ID: {}", ownerId);
            throw new ResourceNotFoundException("User not found with ID: " + ownerId);
        }

        // Map the registration request to a Shop entity
        Shop shop = shopMapper.toEntity(request);
        shop.setOwnerId(ownerId);
        shop.setStatus(ShopStatus.PENDING);

        // Save the shop in the database
        Shop savedShop = shopRepository.save(shop);
        log.info("Shop registered successfully with ID: {}", savedShop.getId());

        // Create an approval request in ShopApprovalService
        shopApprovalFeignClient.createApprovalRequest(savedShop.getId());
        log.info("Approval request created for shop ID: {}", savedShop.getId());

        // Return the response DTO
        return shopMapper.toResponse(savedShop);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponse getShopById(UUID shopId) {
        log.info("Fetching details for shop ID: {}", shopId);
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with ID: " + shopId));
        return shopMapper.toResponse(shop);
    }

    @Override
    @Transactional
    public ShopResponse updateShop(UUID shopId, UpdateShopRequest request) {
        log.info("Updating for shop ID: {} ", shopId);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with ID: " + shopId));

        shopMapper.updateEntity(shop,request);
        // Update the status and save the shop
        Shop updatedShop = shopRepository.save(shop);

        log.info("Shop status updated successfully for ID: {}", updatedShop.getId());
        return shopMapper.toResponse(updatedShop);
    }

    @Override
    @Transactional
    public ShopResponse toggleShopStatus(UUID shopId) {
        log.info("Toggling status for shop ID: {}", shopId);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with ID: " + shopId));

        if (shop.getStatus() == ShopStatus.ACTIVE) {
            shop.setStatus(ShopStatus.INACTIVE);
        } else if (shop.getStatus() == ShopStatus.INACTIVE) {
            shop.setStatus(ShopStatus.ACTIVE);
        }
        else if(shop.getStatus()==ShopStatus.PENDING){
            shop.setStatus(ShopStatus.APPROVED);
        }
        else if(shop.getStatus()==ShopStatus.APPROVED){
            shop.setStatus(ShopStatus.ACTIVE);
        }
        else {
            throw new IllegalStateException("Shop status cannot be toggled from " + shop.getStatus());
        }

        Shop updatedShop = shopRepository.save(shop);
        log.info("Shop status toggled successfully for ID: {}", updatedShop.getId());
        return shopMapper.toResponse(updatedShop);
    }

    @Override
    @Transactional
    public boolean doesShopExist(UUID shopId) {
        return shopRepository.existsById(shopId);
    }

    @Override
    @Transactional
    public ShopBasicInfoDTO getShopBasicInfo(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with ID: " + shopId));
        return shopMapper.toBasicInfo(shop);
    }

    @Override
    @Transactional
    public ShopResponse getShopByOwner(UUID userId) {
        Shop shop = shopRepository.findByOwnerId(userId);
        if(shop==null){
            return null;
        }
        return shopMapper.toResponse(shop);
    }

    @Override
    @Transactional
    public List<ShopResponse> getAllShops() {
        List<Shop> shopEntities = shopRepository.findAll();
        return shopEntities.stream()
                .map(shopMapper::toResponse)
                .toList(); // toList() requires Java 16+, use collect(Collectors.toList()) if on older Java
    }

}

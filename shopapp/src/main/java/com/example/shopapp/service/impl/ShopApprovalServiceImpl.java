package com.example.shopapp.service.impl;

import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.model.*;
import com.example.shopapp.repository.ShopApprovalRepository;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.repository.UserRepository;
import com.example.shopapp.service.ShopApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopApprovalServiceImpl implements ShopApprovalService {
    private final ShopApprovalRepository shopApprovalRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Override
    public ShopApproval createApprovalRequest(Shop shop) {

        ShopApproval approval = new ShopApproval();
        approval.setShop(shop);
        approval.setApprovalStatus(ShopStatus.PENDING);
        approval.setApproved(false); // Default to false
        return shopApprovalRepository.save(approval); // Save approval entry
    }

    @Override
    public ShopApproval approveShop( UUID shopId) {
        ShopApproval approval = shopApprovalRepository.findByShopId(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval for shop with ID " + shopId + " not found"));

        if (approval.getApprovalStatus() != ShopStatus.PENDING) {
            throw new IllegalArgumentException("Cannot approve a shop that is not pending");
        }

        approval.setApprovalStatus(ShopStatus.APPROVED);

        // Update shop status to ACTIVE upon approval
        Shop shop = approval.getShop();
        shop.setStatus(ShopStatus.ACTIVE);
        shopRepository.save(shop); // Save updated shop status

        return shopApprovalRepository.save(approval); // Save approval entry
    }

    @Override
    public ShopApproval rejectShop(UUID shopId, String reason) {
        ShopApproval approval = shopApprovalRepository.findByShopId(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval for shop with ID " + shopId + " not found"));

        if (approval.getApprovalStatus() != ShopStatus.PENDING) {
            throw new IllegalArgumentException("Cannot reject a shop that is not pending");
        }

        // Ensure admin fetched has the admin role

        approval.setApprovalStatus(ShopStatus.REJECTED);
        approval.setReason(reason);
        return shopApprovalRepository.save(approval); // Save updated approval entry
    }

    @Override
    public List<ShopApproval> getPendingApprovals() {
        return shopApprovalRepository.findByApprovalStatus(String.valueOf(ShopStatus.PENDING));
    }

    @Override
    public ShopApproval getApprovalById(UUID approvalId) {
        return shopApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop approval with ID " + approvalId + " not found"));
    }
}

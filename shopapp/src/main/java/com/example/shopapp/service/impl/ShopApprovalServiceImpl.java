package com.example.shopapp.service.impl;

import com.example.shopapp.dto.response.ShopApprovalResponseDTO;
import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopApproval;
import com.example.shopapp.model.ShopStatus;
import com.example.shopapp.repository.ShopApprovalRepository;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.service.ShopApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopApprovalServiceImpl implements ShopApprovalService {

    private final ShopApprovalRepository shopApprovalRepository;
    private final ShopRepository shopRepository;

    @Override
    public ShopApproval createApprovalRequest(Shop shop) {
        log.info("Creating approval request for shop ID: {}", shop.getId());

        ShopApproval approval = new ShopApproval();
        approval.setShop(shop);
        approval.setApprovalStatus(ShopStatus.PENDING);
        approval.setApproved(false);

        ShopApproval savedApproval = shopApprovalRepository.save(approval);
        log.info("Approval request created successfully for shop ID: {}", shop.getId());
        return savedApproval;
    }

    @Override
    public ShopApprovalResponseDTO approveShop(UUID shopId) {
        log.info("Approving shop ID: {}", shopId);

        ShopApproval approval = shopApprovalRepository.findByShopId(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found for shop ID: " + shopId));

        if (!approval.getApprovalStatus().equals(ShopStatus.PENDING)) {
            throw new IllegalArgumentException("Shop is not in a pending state");
        }

        approval.setApprovalStatus(ShopStatus.APPROVED);
        approval.setApproved(true);

        Shop shop = approval.getShop();
        shop.setStatus(ShopStatus.ACTIVE);
        shopRepository.save(shop);

        ShopApproval savedApproval = shopApprovalRepository.save(approval);
        log.info("Shop ID: {} approved successfully", shopId);
        return mapToDTO(savedApproval);
    }

    @Override
    public ShopApprovalResponseDTO rejectShop(UUID shopId, String reason) {
        log.info("Rejecting shop ID: {} with reason: {}", shopId, reason);

        ShopApproval approval = shopApprovalRepository.findByShopId(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found for shop ID: " + shopId));

        if (!approval.getApprovalStatus().equals(ShopStatus.PENDING)) {
            throw new IllegalArgumentException("Shop is not in a pending state");
        }

        approval.setApprovalStatus(ShopStatus.REJECTED);
        approval.setReason(reason);

        ShopApproval savedApproval = shopApprovalRepository.save(approval);
        log.info("Shop ID: {} rejected successfully", shopId);
        return mapToDTO(savedApproval);
    }

    @Override
    public List<ShopApprovalResponseDTO> getPendingApprovals() {
        log.info("Fetching all pending approvals");
        return shopApprovalRepository.findByApprovalStatus(String.valueOf(ShopStatus.PENDING)).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ShopApproval getApprovalById(UUID approvalId) {
        log.info("Fetching approval request ID: {}", approvalId);
        return shopApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found with ID: " + approvalId));
    }

    /**
     * Helper method to map ShopApproval to ShopApprovalResponseDTO.
     *
     * @param approval the ShopApproval entity
     * @return the mapped ShopApprovalResponseDTO
     */
    private ShopApprovalResponseDTO mapToDTO(ShopApproval approval) {
        return new ShopApprovalResponseDTO(
                approval.getId(),
                approval.getShop().getId(),
                approval.getApprovalStatus(),
                approval.getApproved(),
                approval.getReason()
        );
    }
}

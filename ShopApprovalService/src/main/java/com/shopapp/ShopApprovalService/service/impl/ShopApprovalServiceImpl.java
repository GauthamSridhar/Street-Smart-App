package com.shopapp.ShopApprovalService.service.impl;

import com.shopapp.ShopApprovalService.dto.shopapproval.response.ShopApprovalResponseDTO;
import com.shopapp.ShopApprovalService.exception.ResourceNotFoundException;
import com.shopapp.ShopApprovalService.feign.ShopFeignClient;
import com.shopapp.ShopApprovalService.model.ShopApproval;
import com.shopapp.ShopApprovalService.model.ShopStatus;
import com.shopapp.ShopApprovalService.repository.ShopApprovalRepository;
import com.shopapp.ShopApprovalService.service.ShopApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopApprovalServiceImpl implements ShopApprovalService {

    private final ShopApprovalRepository shopApprovalRepository;
    private final ShopFeignClient shopFeignClient;

    @Override
    @Transactional
    public void createApprovalRequest(UUID shopId) {
        log.info("Creating approval request for shop ID: {}", shopId);

        ShopApproval approval = new ShopApproval();
        approval.setShopId(shopId);
        approval.setApprovalStatus(ShopStatus.PENDING);
        approval.setApproved(false);

        shopApprovalRepository.save(approval);
        log.info("Approval request created successfully for shop ID: {}", shopId);
    }

    @Override
    @Transactional
    public ShopApprovalResponseDTO approveShop(UUID shopId) {
        log.info("Approving shop ID: {}", shopId);

        ShopApproval approval = shopApprovalRepository.findByShopId(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found for shop ID: " + shopId));

        if (!approval.getApprovalStatus().equals(ShopStatus.PENDING)) {
            throw new IllegalArgumentException("Shop is not in a pending state");
        }

        // Update the approval status
        approval.setApprovalStatus(ShopStatus.APPROVED);
        approval.setApproved(true);

        // Update the shop status in ShopService
        shopFeignClient.toggleShopStatus(shopId);

        shopApprovalRepository.save(approval);
        log.info("Shop ID: {} approved successfully", shopId);
        return mapToDTO(approval);
    }

    @Override
    @Transactional
    public ShopApprovalResponseDTO rejectShop(UUID shopId, String reason) {
        log.info("Rejecting shop ID: {} with reason: {}", shopId, reason);

        ShopApproval approval = shopApprovalRepository.findByShopId(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found for shop ID: " + shopId));

        if (!approval.getApprovalStatus().equals(ShopStatus.PENDING)) {
            throw new IllegalArgumentException("Shop is not in a pending state");
        }

        // Update the approval status
        approval.setApprovalStatus(ShopStatus.REJECTED);
        approval.setReason(reason);

        // Update the shop status in ShopService
        shopFeignClient.toggleShopStatus(shopId);

        shopApprovalRepository.save(approval);
        log.info("Shop ID: {} rejected successfully", shopId);
        return mapToDTO(approval);
    }

    @Override
    @Transactional
    public List<ShopApprovalResponseDTO> getPendingApprovals() {
        log.info("Fetching all pending approvals");
        return shopApprovalRepository.findByApprovalStatus(ShopStatus.PENDING).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long getPendingApprovalsCount() {
        log.info("Fetching pending approvals count");
        return shopApprovalRepository.countByApprovalStatus(ShopStatus.PENDING);

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
                approval.getShopId(),
                approval.getApprovalStatus(),
                approval.getApproved(),
                approval.getReason()
        );
    }
}

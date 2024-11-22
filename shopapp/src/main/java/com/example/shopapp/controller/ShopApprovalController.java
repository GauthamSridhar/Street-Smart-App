package com.example.shopapp.controller;

import com.example.shopapp.dto.response.ShopApprovalResponseDTO;
import com.example.shopapp.service.ShopApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for shop approval-related operations.
 */
@RestController
@RequestMapping("/api/admin/shop-approvals")
@RequiredArgsConstructor
@Slf4j
public class ShopApprovalController {

    private final ShopApprovalService shopApprovalService;

    /**
     * Approves a shop.
     *
     * @param shopId the shop ID
     * @return the approval response DTO
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{shopId}/approve")
    public ResponseEntity<ShopApprovalResponseDTO> approveShop(@PathVariable UUID shopId) {
        log.info("Admin approving shop ID: {}", shopId);
        ShopApprovalResponseDTO approval = shopApprovalService.approveShop(shopId);
        return ResponseEntity.ok(approval);
    }

    /**
     * Rejects a shop with a reason.
     *
     * @param shopId the shop ID
     * @param reason the reason for rejection
     * @return the rejection response DTO
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{shopId}/reject")
    public ResponseEntity<ShopApprovalResponseDTO> rejectShop(
            @PathVariable UUID shopId,
            @RequestParam String reason) {
        log.info("Admin rejecting shop ID: {} with reason: {}", shopId, reason);
        ShopApprovalResponseDTO rejection = shopApprovalService.rejectShop(shopId, reason);
        return ResponseEntity.ok(rejection);
    }

    /**
     * Retrieves pending shop approvals.
     *
     * @return a list of pending approvals
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<ShopApprovalResponseDTO>> getPendingApprovals() {
        log.info("Fetching pending shop approvals");
        List<ShopApprovalResponseDTO> pendingApprovals = shopApprovalService.getPendingApprovals();
        return ResponseEntity.ok(pendingApprovals);
    }
}

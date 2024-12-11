package com.shopapp.ShopApprovalService.controller;

import com.shopapp.ShopApprovalService.dto.shopapproval.response.ShopApprovalResponseDTO;
import com.shopapp.ShopApprovalService.service.ShopApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Slf4j
public class ShopApprovalController {

    private final ShopApprovalService shopApprovalService;

    @PostMapping("/create")
    public ResponseEntity<Void> createApprovalRequest(@RequestParam("shopId") UUID shopId) {
        log.info("Received approval request for shop ID: {}", shopId);
        shopApprovalService.createApprovalRequest(shopId);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/{shopId}/approve")
    public ResponseEntity<ShopApprovalResponseDTO> approveShop(@PathVariable UUID shopId) {
        log.info("Approving shop ID: {}", shopId);
        ShopApprovalResponseDTO response = shopApprovalService.approveShop(shopId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{shopId}/reject")
    public ResponseEntity<ShopApprovalResponseDTO> rejectShop(@PathVariable UUID shopId, @RequestParam String reason) {
        log.info("Rejecting shop ID: {} with reason: {}", shopId, reason);
        ShopApprovalResponseDTO response = shopApprovalService.rejectShop(shopId, reason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ShopApprovalResponseDTO>> getPendingApprovals() {
        log.info("Fetching pending approvals");
        List<ShopApprovalResponseDTO> pendingApprovals = shopApprovalService.getPendingApprovals();
        return ResponseEntity.ok(pendingApprovals);
    }
    //count of pending status shops
    @GetMapping("/pending/count")
    public ResponseEntity<Long> getPendingApprovalsCount() {
        log.info("Fetching pending approvals count");
        Long count = shopApprovalService.getPendingApprovalsCount();
        return ResponseEntity.ok(count);
    }

}

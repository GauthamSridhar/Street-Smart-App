package com.example.shopapp.controller;

import com.example.shopapp.model.ShopApproval;
import com.example.shopapp.service.ShopApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shop-approvals")
@RequiredArgsConstructor
public class ShopApprovalController {
    private final ShopApprovalService shopApprovalService;

    @PostMapping("/{shopId}/approve")
    public ResponseEntity<ShopApproval> approveShop(
            @PathVariable UUID shopId,
            @RequestParam UUID adminId) {
        ShopApproval approval = shopApprovalService.approveShop(adminId, shopId);
        return ResponseEntity.ok(approval);
    }

    @PostMapping("/{shopId}/reject")
    public ResponseEntity<ShopApproval> rejectShop(
            @PathVariable UUID shopId,
            @RequestParam UUID adminId,
            @RequestParam String reason) {
        ShopApproval approval = shopApprovalService.rejectShop(adminId, shopId, reason);
        return ResponseEntity.ok(approval);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ShopApproval>> getPendingApprovals() {
        List<ShopApproval> approvals = shopApprovalService.getPendingApprovals();
        return ResponseEntity.ok(approvals);
    }

    @GetMapping("/{approvalId}")
    public ResponseEntity<ShopApproval> getApprovalById(@PathVariable UUID approvalId) {
        ShopApproval approval = shopApprovalService.getApprovalById(approvalId);
        return ResponseEntity.ok(approval);
    }
}
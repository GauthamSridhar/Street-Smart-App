package com.example.shopapp.controller;

import com.example.shopapp.dto.response.ShopApprovalResponseDTO;
import com.example.shopapp.mapper.ShopApprovalMapper;
import com.example.shopapp.model.ShopApproval;
import com.example.shopapp.service.ShopApprovalService;
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
    private final ShopApprovalMapper shopApprovalMapper;

    @PostMapping("/{shopId}/approve")
    public ResponseEntity<ShopApprovalResponseDTO> approveShop(
            @PathVariable UUID shopId) {
        ShopApproval approval = shopApprovalService.approveShop(shopId);
        return ResponseEntity.ok(shopApprovalMapper.toDTO(approval));
    }

    @PostMapping("/{shopId}/reject")
    public ResponseEntity<ShopApprovalResponseDTO> rejectShop(
            @PathVariable UUID shopId,
            @RequestParam String reason) {
        ShopApproval approval = shopApprovalService.rejectShop(shopId, reason);
        return ResponseEntity.ok(shopApprovalMapper.toDTO(approval));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ShopApprovalResponseDTO>> getPendingApprovals() {
        List<ShopApproval> approvals = shopApprovalService.getPendingApprovals();
        return ResponseEntity.ok(shopApprovalMapper.toDTOList(approvals));
    }

    @GetMapping("/{approvalId}")
    public ResponseEntity<ShopApprovalResponseDTO> getApprovalById(
            @PathVariable UUID approvalId) {
        ShopApproval approval = shopApprovalService.getApprovalById(approvalId);
        return ResponseEntity.ok(shopApprovalMapper.toDTO(approval));
    }
}

package com.example.shopapp.service;

import com.example.shopapp.dto.response.ShopApprovalResponseDTO;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopApproval;

import java.util.List;
import java.util.UUID;

public interface ShopApprovalService {
    ShopApproval createApprovalRequest(Shop registeredShop);

    ShopApprovalResponseDTO approveShop(UUID shopId);

    ShopApprovalResponseDTO rejectShop(UUID shopId, String reason);

    List<ShopApprovalResponseDTO> getPendingApprovals();
    ShopApproval getApprovalById(UUID approvalId);
}

package com.example.shopapp.service;

import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopApproval;

import java.util.List;
import java.util.UUID;

public interface ShopApprovalService {
    ShopApproval createApprovalRequest(Shop registeredShop, UUID adminId); // Update the return type
    ShopApproval approveShop(UUID adminId, UUID shopId);
    ShopApproval rejectShop(UUID adminId, UUID shopId, String reason);
    List<ShopApproval> getPendingApprovals();
    ShopApproval getApprovalById(UUID approvalId);
}

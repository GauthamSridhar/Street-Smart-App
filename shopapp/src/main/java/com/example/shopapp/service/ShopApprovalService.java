package com.example.shopapp.service;

import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopApproval;

import java.util.List;
import java.util.UUID;

public interface ShopApprovalService {
    ShopApproval createApprovalRequest(Shop registeredShop ); // Update the return type
    ShopApproval approveShop( UUID shopId);
    ShopApproval rejectShop( UUID shopId, String reason);
    List<ShopApproval> getPendingApprovals();
    ShopApproval getApprovalById(UUID approvalId);
}

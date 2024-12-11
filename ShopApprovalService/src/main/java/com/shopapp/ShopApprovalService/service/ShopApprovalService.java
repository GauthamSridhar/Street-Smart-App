package com.shopapp.ShopApprovalService.service;

import com.shopapp.ShopApprovalService.dto.shopapproval.response.ShopApprovalResponseDTO;
import com.shopapp.ShopApprovalService.model.ShopApproval;
import com.shopapp.ShopApprovalService.model.ShopStatus;

import java.util.List;
import java.util.UUID;

public interface ShopApprovalService {

    void createApprovalRequest(UUID shopId);

    ShopApprovalResponseDTO approveShop(UUID shopId);

    ShopApprovalResponseDTO rejectShop(UUID shopId, String reason);

    List<ShopApprovalResponseDTO> getPendingApprovals();

    Long getPendingApprovalsCount();
}

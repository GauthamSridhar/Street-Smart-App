package com.shopapp.ShopApprovalService.service;

import com.shopapp.ShopApprovalService.dto.shopapproval.response.ShopApprovalResponseDTO;
import java.util.List;
import java.util.UUID;

public interface ShopApprovalService {

  void createApprovalRequest(UUID shopId);

  ShopApprovalResponseDTO approveShop(UUID shopId);

  ShopApprovalResponseDTO rejectShop(UUID shopId, String reason);

  List<ShopApprovalResponseDTO> getPendingApprovals();

  Long getPendingApprovalsCount();

  List<ShopApprovalResponseDTO> pendingPage(int page, int size);
}

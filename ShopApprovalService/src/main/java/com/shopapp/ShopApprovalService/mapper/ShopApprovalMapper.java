package com.shopapp.ShopApprovalService.mapper;

import com.shopapp.ShopApprovalService.dto.shopapproval.response.ShopApprovalResponseDTO;
import com.shopapp.ShopApprovalService.model.ShopApproval;
import org.springframework.stereotype.Component;

@Component
public class ShopApprovalMapper {
  public ShopApprovalResponseDTO toDTO(ShopApproval a) {
    var dto = new ShopApprovalResponseDTO();
    dto.setId(a.getId());
    dto.setShopId(a.getShopId());
    dto.setApprovalStatus(a.getApprovalStatus());
    dto.setApproved(a.getApproved());
    dto.setReason(a.getReason());
    dto.setDecidedBy(a.getDecidedBy());
    dto.setDecidedAt(a.getDecidedAt());
    dto.setDeliveryPending(!a.isSynchronizedWithShop());
    return dto;
  }
}

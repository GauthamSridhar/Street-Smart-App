package com.shopapp.ShopApprovalService.dto.shopapproval.response;

import com.shopapp.ShopApprovalService.model.ShopStatus;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopApprovalResponseDTO {
  private UUID id;
  private UUID shopId;
  private ShopStatus approvalStatus;
  private Boolean approved;
  private String reason;
  private UUID decidedBy;
  private java.time.LocalDateTime decidedAt;
  private boolean deliveryPending;
}

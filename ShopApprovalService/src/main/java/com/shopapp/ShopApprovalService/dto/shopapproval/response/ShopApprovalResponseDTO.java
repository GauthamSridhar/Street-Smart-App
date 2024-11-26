package com.shopapp.ShopApprovalService.dto.shopapproval.response;

import com.shopapp.ShopApprovalService.model.ShopStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopApprovalResponseDTO {
    private UUID id;
    private UUID shopId;
    private ShopStatus approvalStatus;
    private Boolean approved;
    private String reason;
}

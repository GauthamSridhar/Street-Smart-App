package com.example.shopapp.dto.response;

import com.example.shopapp.model.ShopStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopApprovalResponseDTO {
    private UUID id;             // Approval ID
    private UUID shopId;         // ID of the associated shop
    private ShopStatus approvalStatus; // Status of the shop approval
    private Boolean approved;    // Indicates if the shop is approved
    private String reason;       // Reason for rejection (if applicable)
}

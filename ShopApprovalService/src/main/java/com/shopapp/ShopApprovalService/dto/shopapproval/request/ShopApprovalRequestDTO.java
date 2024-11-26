package com.shopapp.ShopApprovalService.dto.shopapproval.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ShopApprovalRequestDTO {
    @NotNull(message = "Shop ID cannot be null")
    private UUID shopId;

    private String reason; // Used only in rejection cases
}

package com.example.shopapp.dto.response;

import com.example.shopapp.model.ShopStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ShopApprovalResponseDTO {
    private UUID id;
    private UUID shopId;
    private ShopStatus approvalStatus;
    private Boolean approved;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}

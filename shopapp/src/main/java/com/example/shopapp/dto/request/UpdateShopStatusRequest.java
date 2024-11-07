package com.example.shopapp.dto.request;

import com.example.shopapp.model.ShopStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UpdateShopStatusRequest {
    @NotNull(message = "Status is required")
    private ShopStatus status;
}
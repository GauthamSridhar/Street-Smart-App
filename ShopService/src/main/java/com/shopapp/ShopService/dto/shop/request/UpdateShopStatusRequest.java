package com.shopapp.ShopService.dto.shop.request;

import com.shopapp.ShopService.model.ShopStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class UpdateShopStatusRequest {
    @NotNull(message = "Status is required")
    private ShopStatus status;
}
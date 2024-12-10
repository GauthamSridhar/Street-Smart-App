package com.shopapp.ShopService.dto.product.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductResponseDTO {
    private UUID id;
    private String name;
    private boolean available;
    private UUID shopId;
}

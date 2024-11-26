package com.shopapp.ShopService.dto.product.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean available;
    private UUID shopId;
}

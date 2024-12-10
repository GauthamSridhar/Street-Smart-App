package com.shopapp.ShopService.dto.product.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddProductRequest {
    @NotBlank
    private String name;

    private boolean available;
}

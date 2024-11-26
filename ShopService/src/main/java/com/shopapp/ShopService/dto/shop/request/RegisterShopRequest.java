package com.shopapp.ShopService.dto.shop.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterShopRequest {
    @NotBlank(message = "Shop name is required")
    @Size(min = 3, max = 100, message = "Shop name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    private Double latitude;
    private Double longitude;
}
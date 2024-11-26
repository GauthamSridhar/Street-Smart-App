package com.shopapp.ShopService.dto.image.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImageUploadDTO {
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
}
package com.example.shopapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImageUploadDTO {
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
}
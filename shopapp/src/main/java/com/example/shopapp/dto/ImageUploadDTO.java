package com.example.shopapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class ImageUploadDTO {
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
}
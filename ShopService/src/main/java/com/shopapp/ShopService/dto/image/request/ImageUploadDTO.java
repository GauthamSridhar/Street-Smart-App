package com.shopapp.ShopService.dto.image.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ImageUploadDTO {
    @NotNull(message = "Image data is required")
    private byte[] imageData;

    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "File type is required")
    private String fileType;
}

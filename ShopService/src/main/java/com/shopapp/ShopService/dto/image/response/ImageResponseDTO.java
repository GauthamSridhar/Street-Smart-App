package com.shopapp.ShopService.dto.image.response;

import lombok.Data;

import java.util.UUID;

@Data
public class ImageResponseDTO {
    private UUID id;
    private String imageUrl;
    private UUID shopId;
}
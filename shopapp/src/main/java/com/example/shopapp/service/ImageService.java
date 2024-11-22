package com.example.shopapp.service;

import com.example.shopapp.dto.request.ImageUploadDTO;
import com.example.shopapp.dto.response.ShopRatingsSummary;

import java.util.List;
import java.util.UUID;

public interface ImageService {
    ShopRatingsSummary.ImageResponseDTO uploadImage(UUID shopId, ImageUploadDTO imageDTO);
    void deleteImage(UUID imageId);
    List<ShopRatingsSummary.ImageResponseDTO> getImagesByShop(UUID shopId);
}

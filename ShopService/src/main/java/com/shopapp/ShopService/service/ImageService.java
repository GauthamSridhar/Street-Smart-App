package com.shopapp.ShopService.service;

import com.shopapp.ShopService.dto.image.request.ImageUploadDTO;
import com.shopapp.ShopService.dto.image.response.ImageResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ImageService {
    ImageResponseDTO uploadImage(UUID shopId, ImageUploadDTO imageDTO);
    void deleteImage(UUID imageId);
    List<ImageResponseDTO> getImagesByShop(UUID shopId);
}

package com.example.shopapp.service;

import com.example.shopapp.dto.ImageResponseDTO;
import com.example.shopapp.dto.ImageUploadDTO;

import java.util.List;
import java.util.UUID;

public interface ImageService {
    ImageResponseDTO uploadImage(UUID shopId, ImageUploadDTO imageDTO);
    void deleteImage(UUID imageId);
    List<ImageResponseDTO> getImagesByShop(UUID shopId);
}

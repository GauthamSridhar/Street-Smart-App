package com.shopapp.ShopService.service;

import com.shopapp.ShopService.dto.image.response.ImageResponseDTO;
import com.shopapp.ShopService.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ImageService {
    ImageResponseDTO uploadImage(UUID shopId, MultipartFile file);
    void deleteImage(UUID imageId);
    List<ImageResponseDTO> getImagesByShop(UUID shopId);
    Image getImageById(UUID imageId);
}

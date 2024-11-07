

package com.example.shopapp.mapper;

import com.example.shopapp.dto.ImageResponseDTO;
import com.example.shopapp.dto.ImageUploadDTO;
import com.example.shopapp.model.Image;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper {

    public Image toEntity(ImageUploadDTO dto) {
        Image image = new Image();
        image.setImageUrl(dto.getImageUrl());
        return image;
    }

    public ImageResponseDTO toDTO(Image image) {
        ImageResponseDTO dto = new ImageResponseDTO();
        dto.setId(image.getId());
        dto.setImageUrl(image.getImageUrl());
        dto.setShopId(image.getShop().getId());
        return dto;
    }
}
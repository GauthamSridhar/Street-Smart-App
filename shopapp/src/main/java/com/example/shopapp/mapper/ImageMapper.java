

package com.example.shopapp.mapper;

import com.example.shopapp.dto.request.ImageUploadDTO;
import com.example.shopapp.dto.response.ShopRatingsSummary;
import com.example.shopapp.model.Image;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper {

    public Image toEntity(ImageUploadDTO dto) {
        Image image = new Image();
        image.setImageUrl(dto.getImageUrl());
        return image;
    }

    public ShopRatingsSummary.ImageResponseDTO toDTO(Image image) {
        ShopRatingsSummary.ImageResponseDTO dto = new ShopRatingsSummary.ImageResponseDTO();
        dto.setId(image.getId());
        dto.setImageUrl(image.getImageUrl());
        dto.setShopId(image.getShop().getId());
        return dto;
    }
}


package com.shopapp.ShopService.mapper;


import com.shopapp.ShopService.dto.image.request.ImageUploadDTO;
import com.shopapp.ShopService.dto.image.response.ImageResponseDTO;
import com.shopapp.ShopService.model.Image;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper {

    public Image toEntity(ImageUploadDTO dto) {
        Image image = new Image();
        image.setUrl(dto.getImageUrl());
        return image;
    }

    public ImageResponseDTO toDTO(Image image) {
        ImageResponseDTO dto = new ImageResponseDTO();
        dto.setId(image.getId());
        dto.setImageUrl(image.getUrl());
        dto.setShopId(image.getShop().getId());
        return dto;
    }
}
package com.shopapp.ShopService.mapper;

import com.shopapp.ShopService.dto.image.request.ImageUploadDTO;
import com.shopapp.ShopService.dto.image.response.ImageResponseDTO;
import com.shopapp.ShopService.model.Image;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper {

    public Image toEntity(ImageUploadDTO dto) {
        Image image = new Image();
        image.setImageData(dto.getImageData());
        image.setFileName(dto.getFileName());
        image.setFileType(dto.getFileType());
        // The shop should be set separately in the service
        return image;
    }

    public ImageResponseDTO toDTO(Image image) {
        ImageResponseDTO dto = new ImageResponseDTO();
        dto.setId(image.getId());
        dto.setFileName(image.getFileName());
        dto.setFileType(image.getFileType());
        dto.setShopId(image.getShop().getId());
        dto.setFileSizeInBytes(image.getImageData().length);
        return dto;
    }
}

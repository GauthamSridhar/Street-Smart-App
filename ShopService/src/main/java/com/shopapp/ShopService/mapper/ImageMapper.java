package com.shopapp.ShopService.mapper;

import com.shopapp.ShopService.dto.image.response.ImageResponseDTO;
import com.shopapp.ShopService.model.Image;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper {
  public ImageResponseDTO toDTO(Image image) {
    var dto = new ImageResponseDTO();
    dto.setId(image.getId());
    dto.setFileName(image.getFileName());
    dto.setFileType(image.getFileType());
    dto.setShopId(image.getShop().getId());
    dto.setFileSizeInBytes(image.getFileSizeInBytes());
    return dto;
  }
}

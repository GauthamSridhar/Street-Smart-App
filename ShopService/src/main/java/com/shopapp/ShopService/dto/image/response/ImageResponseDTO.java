package com.shopapp.ShopService.dto.image.response;

import java.util.UUID;
import lombok.Data;

@Data
public class ImageResponseDTO {
  private UUID id;
  private String fileName;
  private String fileType;
  private UUID shopId;
  private long fileSizeInBytes;
}

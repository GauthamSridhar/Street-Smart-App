package com.shopapp.ShopService.dto.product.response;

import java.util.UUID;
import lombok.Data;

@Data
public class ProductResponseDTO {
  private UUID id;
  private String name;
  private boolean available;
  private UUID shopId;
  private String description;
  private java.math.BigDecimal price;
  private String currency;
  private java.time.Instant updatedAt;
}

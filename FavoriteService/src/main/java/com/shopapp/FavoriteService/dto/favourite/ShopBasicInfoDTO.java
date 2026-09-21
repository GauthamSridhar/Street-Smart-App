// Define ShopBasicInfoDTO inside FavoriteService
package com.shopapp.FavoriteService.dto.favourite;

import java.util.UUID;
import lombok.Data;

@Data
public class ShopBasicInfoDTO {
  private UUID id;
  private String name;
}

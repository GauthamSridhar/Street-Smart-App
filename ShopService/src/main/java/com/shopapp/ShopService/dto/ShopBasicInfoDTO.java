package com.shopapp.ShopService.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopBasicInfoDTO {
  private UUID id;
  private String name;
}

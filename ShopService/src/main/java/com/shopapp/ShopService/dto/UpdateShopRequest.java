package com.shopapp.ShopService.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateShopRequest {
  @NotBlank
  @Size(min = 3, max = 100)
  private String name;

  @NotBlank
  @Size(min = 10, max = 1000)
  private String description;

  @NotBlank
  @Size(max = 255)
  private String address;

  @NotBlank
  @Size(max = 60)
  private String category;

  @Size(max = 255)
  private String openingHours;

  @NotNull
  @DecimalMin("-90")
  @DecimalMax("90")
  private Double latitude;

  @NotNull
  @DecimalMin("-180")
  @DecimalMax("180")
  private Double longitude;
}

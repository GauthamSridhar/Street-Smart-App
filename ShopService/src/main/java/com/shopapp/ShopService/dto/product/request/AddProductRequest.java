package com.shopapp.ShopService.dto.product.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddProductRequest {
  @NotBlank
  @jakarta.validation.constraints.Size(max = 255)
  private String name;

  private boolean available;

  @jakarta.validation.constraints.Size(max = 1000)
  private String description;

  @jakarta.validation.constraints.DecimalMin("0.00")
  @jakarta.validation.constraints.Digits(integer = 10, fraction = 2)
  private java.math.BigDecimal price;

  @jakarta.validation.constraints.Pattern(regexp = "[A-Z]{3}")
  private String currency;

  @jakarta.validation.constraints.AssertTrue(
      message = "Price and currency must be supplied together")
  public boolean isPriceCurrencyValid() {
    return (price == null) == (currency == null);
  }
}

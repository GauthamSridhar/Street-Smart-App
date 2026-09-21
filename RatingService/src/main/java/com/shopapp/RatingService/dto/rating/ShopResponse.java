package com.shopapp.RatingService.dto.rating;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopResponse {
  private UUID id;
  private UUID ownerId;
  private String status;
}

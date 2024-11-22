package com.example.shopapp.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

/**
 * Detailed response DTO for shop information, including products and ratings.
 */
@Data
public class ShopDetailResponse  {
    private UUID id;
    private List<ProductResponseDTO> products;
    private List<RatingResponseDTO> ratings;
}

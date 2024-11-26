package com.shopapp.ShopService.dto.shop.response;

import com.shopapp.ShopService.dto.product.response.ProductResponseDTO;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Detailed response DTO for shop information, including products and ratings.
 */
@Data
public class ShopDetailResponse  {
    private UUID id;
    private List<ProductResponseDTO> products;
    private List<UUID> ratings;
}

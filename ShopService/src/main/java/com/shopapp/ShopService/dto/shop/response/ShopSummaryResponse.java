package com.shopapp.ShopService.dto.shop.response;


import com.shopapp.ShopService.dto.image.response.ImageResponseDTO;
import com.shopapp.ShopService.model.ShopStatus;
import lombok.Data;

import java.util.UUID;

/**
 * Summarized response DTO for shop listings.
 */
@Data
public class ShopSummaryResponse {

    private UUID id;
    private String name;
    private String description;
    private String address;
    private ShopStatus status;
    private Double averageRating;
    private ImageResponseDTO primaryImage;
}

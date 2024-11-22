package com.example.shopapp.dto.response;

import com.example.shopapp.model.ShopStatus;
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
    private ShopRatingsSummary.ImageResponseDTO primaryImage;
}

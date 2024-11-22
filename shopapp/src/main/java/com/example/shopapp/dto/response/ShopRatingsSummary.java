package com.example.shopapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for summarizing shop ratings.
 */
@Data
@Builder
public class ShopRatingsSummary {
    private UUID shopId;
    private Double averageRating;
    private Integer totalRatings;
    private Map<Integer, Integer> ratingDistribution;
    private List<RatingResponseDTO> recentRatings;

    @Data
    public static class ImageResponseDTO {
        private UUID id;
        private String imageUrl;
        private UUID shopId;
    }
}

package com.shopapp.ShopService.dto.shop.response;

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
    private List<UUID> recentRatings;

}

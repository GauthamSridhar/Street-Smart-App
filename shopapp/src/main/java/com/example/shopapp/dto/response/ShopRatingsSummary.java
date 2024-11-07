package com.example.shopapp.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ShopRatingsSummary {
    private Float averageRating;
    private Integer totalRatings;
    private Map<Integer, Integer> ratingDistribution;
    private List<RatingResponseDTO> recentRatings;
}

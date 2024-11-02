// service/RatingService.java
package com.example.shopapp.service;

import com.example.shopapp.model.Rating;

import java.util.List;
import java.util.UUID;

public interface RatingService {
    Rating addRating(UUID userId, UUID shopId, Rating request);
    Rating updateRating(UUID userId, UUID ratingId, Rating request);
    void deleteRating(UUID userId, UUID ratingId);
    List<Rating> getShopRatings(UUID shopId); // Assuming ShopRatingsSummary is still needed

    Rating getRating(UUID ratingId);
}

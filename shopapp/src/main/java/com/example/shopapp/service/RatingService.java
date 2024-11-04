package com.example.shopapp.service;

import com.example.shopapp.model.Rating;
import java.util.List;
import java.util.UUID;

public interface RatingService {
    Rating addRating(UUID userId, UUID shopId, Rating rating);
    Rating getRating(UUID ratingId);
    Rating updateRating(UUID userId, UUID ratingId, Rating rating);
    void deleteRating(UUID userId, UUID ratingId);
    List<Rating> getShopRatings(UUID shopId);
}

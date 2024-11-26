package com.shopapp.RatingService.service;



import com.shopapp.RatingService.dto.rating.request.RatingCreateDTO;
import com.shopapp.RatingService.dto.rating.request.RatingUpdateDTO;
import com.shopapp.RatingService.dto.rating.response.RatingResponseDTO;

import java.util.List;
import java.util.UUID;

public interface RatingService {
    RatingResponseDTO addRating(UUID userId, UUID shopId, RatingCreateDTO ratingDTO);
    RatingResponseDTO updateRating(UUID userId, UUID ratingId, RatingUpdateDTO ratingDTO);
    void deleteRating(UUID userId, UUID ratingId);
    List<RatingResponseDTO> getShopRatings(UUID shopId);
    RatingResponseDTO getRating(UUID ratingId);
}

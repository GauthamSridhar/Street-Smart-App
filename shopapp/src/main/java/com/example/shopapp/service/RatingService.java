package com.example.shopapp.service;

import com.example.shopapp.dto.request.RatingCreateDTO;
import com.example.shopapp.dto.request.RatingUpdateDTO;
import com.example.shopapp.dto.response.RatingResponseDTO;

import java.util.List;
import java.util.UUID;

public interface RatingService {
    RatingResponseDTO addRating(UUID userId, UUID shopId, RatingCreateDTO ratingDTO);
    RatingResponseDTO updateRating(UUID userId, UUID ratingId, RatingUpdateDTO ratingDTO);
    void deleteRating(UUID userId, UUID ratingId);
    List<RatingResponseDTO> getShopRatings(UUID shopId);
    RatingResponseDTO getRating(UUID ratingId);
}

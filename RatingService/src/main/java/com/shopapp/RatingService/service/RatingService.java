package com.shopapp.RatingService.service;

import com.shopapp.RatingService.dto.rating.request.RatingCreateDTO;
import com.shopapp.RatingService.dto.rating.request.RatingUpdateDTO;
import com.shopapp.RatingService.dto.rating.response.RatingResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

public interface RatingService {
  RatingResponseDTO addRating(
      UUID userId, UUID shopId, RatingCreateDTO ratingDTO, HttpServletRequest req);

  RatingResponseDTO updateRating(UUID userId, UUID ratingId, RatingUpdateDTO ratingDTO);

  void deleteRating(UUID userId, UUID ratingId, UUID shopId, HttpServletRequest request);

  List<RatingResponseDTO> getShopRatings(UUID shopId);

  RatingResponseDTO getRating(UUID ratingId);

  Long getShopRatingsCount(UUID shopId);

  java.util.Map<String, Number> summary(UUID shopId);

  List<RatingResponseDTO> getShopRatingsPage(UUID shopId, int page, int size);

  java.util.Map<String, Object> report(UUID ratingId, String reason);

  List<java.util.Map<String, Object>> reports(String status);

  void resolveReport(UUID reportId, String action);
}

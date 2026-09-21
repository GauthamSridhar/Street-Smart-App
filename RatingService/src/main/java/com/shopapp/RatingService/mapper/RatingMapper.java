package com.shopapp.RatingService.mapper;

import com.shopapp.RatingService.dto.rating.request.*;
import com.shopapp.RatingService.dto.rating.response.RatingResponseDTO;
import com.shopapp.RatingService.model.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {
  public Rating toEntity(RatingCreateDTO request) {
    var rating = new Rating();
    rating.setRating(request.getRating());
    rating.setReview(request.getReview());
    return rating;
  }

  public void updateEntity(Rating rating, RatingUpdateDTO request) {
    rating.setRating(request.getRating());
    rating.setReview(request.getReview());
  }

  public RatingResponseDTO toDTO(Rating rating) {
    var dto = new RatingResponseDTO();
    dto.setId(rating.getId());
    dto.setRating(rating.getRating());
    dto.setReview(rating.getReview());
    dto.setUserId(rating.getUserId().toString());
    dto.setShopId(rating.getShopId().toString());
    dto.setUpdatedAt(rating.getUpdatedAt() == null ? null : rating.getUpdatedAt().toString());
    return dto;
  }
}

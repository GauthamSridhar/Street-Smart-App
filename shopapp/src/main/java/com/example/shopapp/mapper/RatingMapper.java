package com.example.shopapp.mapper;

import com.example.shopapp.dto.request.RatingCreateDTO;
import com.example.shopapp.dto.request.RatingUpdateDTO;
import com.example.shopapp.dto.response.RatingResponseDTO; // Updated to use RatingResponseDTO
import com.example.shopapp.model.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {

    public Rating toEntity(RatingCreateDTO dto) {
        Rating rating = new Rating();
        rating.setRating(dto.getRating());
        rating.setReview(dto.getReview());
        return rating;
    }

    public void updateEntity(Rating rating, RatingUpdateDTO dto) {
        rating.setRating(dto.getRating());
        rating.setReview(dto.getReview());
    }

    public RatingResponseDTO toDTO(Rating rating) { // Updated to return RatingResponseDTO
        RatingResponseDTO dto = new RatingResponseDTO();
        dto.setId(rating.getId());
//        dto.setUserId(rating.getUser().getId());
//        dto.setShopId(rating.getShop().getId());
        dto.setRating(rating.getRating());
        dto.setReview(rating.getReview());
//        dto.setCreatedAt(rating.getCreatedAt());
//        dto.setUpdatedAt(rating.getUpdatedAt());

        // Add additional useful information
//        dto.setUserName(rating.getUser().getFullName());  // Assuming User has getFullName() method
//        dto.setShopName(rating.getShop().getName());      // Assuming Shop has getName() method

        return dto;
    }
}

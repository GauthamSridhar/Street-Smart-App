package com.shopapp.RatingService.dto.rating.response;

import lombok.Data;

import java.util.UUID;

@Data
public  class RatingResponseDTO {
    private UUID id;
    private Integer rating;
    private String review;
    private String userId;
    private String ShopId;
    private String UpdatedAt;

}
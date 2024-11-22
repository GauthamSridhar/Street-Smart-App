package com.example.shopapp.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public  class RatingResponseDTO {
    private UUID id;
//    private UUID userId;
//    private UUID shopId;
    private Integer rating;
    private String review;
//
//    // Additional fields that might be useful for the client
//    private String userName;  // User's name who left the rating
//    private String shopName; // Shop name that was rated
}
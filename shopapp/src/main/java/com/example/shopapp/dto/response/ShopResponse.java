// dto/response/ShopResponse.java
package com.example.shopapp.dto.response;

import com.example.shopapp.model.ShopStatus;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ShopResponse {
    private UUID id;
    private String name;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private ShopStatus status;
    private UserResponse owner;
    private Double averageRating;
    private List<ShopRatingsSummary.ImageResponseDTO> images;
}

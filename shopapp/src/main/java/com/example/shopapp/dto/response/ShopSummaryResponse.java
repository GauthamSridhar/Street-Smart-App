package com.example.shopapp.dto.response;

import com.example.shopapp.dto.ImageResponseDTO;
import com.example.shopapp.model.ShopStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class ShopSummaryResponse {
    private UUID id;
    private String name;
    private String description;
    private String address;
    private ShopStatus status;
    private Double averageRating;
    private ImageResponseDTO primaryImage;
}

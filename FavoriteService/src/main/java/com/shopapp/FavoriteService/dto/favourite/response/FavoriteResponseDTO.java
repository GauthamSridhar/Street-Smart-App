// FavoriteResponseDTO.java
package com.shopapp.FavoriteService.dto.favourite.response;

import lombok.Data;

import java.util.UUID;

@Data
public class FavoriteResponseDTO {
    private UUID id;
    private UUID userId;
    private UUID shopId;
    private String shopName;  // Including basic shop info that might be useful for UI

    // You might want to add more shop-related fields here depending on your UI needs
}
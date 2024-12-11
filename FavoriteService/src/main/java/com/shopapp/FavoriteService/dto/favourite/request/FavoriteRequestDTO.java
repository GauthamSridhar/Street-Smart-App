package com.shopapp.FavoriteService.dto.favourite.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FavoriteRequestDTO {
    @NotNull(message = "User ID cannot be null")
    private UUID userId;

    @NotNull(message = "Shop ID cannot be null")
    private UUID shopId;

    String rating;;
    String review;
}
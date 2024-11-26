package com.shopapp.FavoriteService.service;

import com.shopapp.FavoriteService.dto.favourite.response.FavoriteResponseDTO;

import java.util.List;
import java.util.UUID;

public interface FavoriteService {

    FavoriteResponseDTO addFavorite(UUID userId, UUID shopId);

    void removeFavorite(UUID userId, UUID shopId);

    List<FavoriteResponseDTO> getFavoritesByUser(UUID userId);

    boolean isFavorite(UUID userId, UUID shopId);
}

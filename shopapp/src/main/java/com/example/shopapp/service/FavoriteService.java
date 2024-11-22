package com.example.shopapp.service;

import com.example.shopapp.dto.response.FavoriteResponseDTO;
import com.example.shopapp.model.Favorite;
import java.util.List;
import java.util.UUID;

public interface FavoriteService {
    FavoriteResponseDTO addFavorite(UUID userId, UUID shopId);
    void removeFavorite(UUID userId, UUID shopId);
    List<FavoriteResponseDTO> getFavoritesByUser(UUID userId);
    boolean isFavorite(UUID userId, UUID shopId);
}

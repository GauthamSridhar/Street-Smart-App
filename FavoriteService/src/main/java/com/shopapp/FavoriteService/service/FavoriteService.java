package com.shopapp.FavoriteService.service;

import com.shopapp.FavoriteService.dto.favourite.response.FavoriteResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface FavoriteService {

    FavoriteResponseDTO addFavorite(UUID userId, UUID shopId, HttpServletRequest req);

    void removeFavorite(UUID userId, UUID shopId, HttpServletRequest req);

    List<FavoriteResponseDTO> getFavoritesByUser(UUID userId,HttpServletRequest req);

    boolean isFavorite(UUID userId, UUID shopId);

    int getFavoriteCount(UUID userId, HttpServletRequest request);
}

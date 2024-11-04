package com.example.shopapp.service;

import com.example.shopapp.model.Favorite;
import java.util.List;
import java.util.UUID;

public interface FavoriteService {
    Favorite addFavorite(UUID userId, UUID shopId);
    void removeFavorite(UUID userId, UUID shopId);
    List<Favorite> getFavoritesByUser(UUID userId);
    boolean isFavorite(UUID userId, UUID shopId);
}

package com.example.shopapp.service;

import com.example.shopapp.model.Favourite;
import java.util.List;
import java.util.UUID;

public interface FavouriteService {
    Favourite addFavourite(UUID userId, UUID shopId);
    void removeFavourite(UUID userId, UUID shopId);
    List<Favourite> getFavouritesByUser(UUID userId);
    boolean isFavourite(UUID userId, UUID shopId);
}

package com.example.shopapp.service.impl;

import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.model.Favorite;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.User;
import com.example.shopapp.repository.FavouriteRepository;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.repository.UserRepository;
import com.example.shopapp.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    private final FavouriteRepository favouriteRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Override
    public Favorite addFavorite(UUID userId, UUID shopId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop with ID " + shopId + " not found"));

        Favorite favourite = new Favorite();
        favourite.setUser(user);
        favourite.setShop(shop);
        return favouriteRepository.save(favourite);
    }

    @Override
    public void removeFavorite(UUID userId, UUID shopId) {
        if (!favouriteRepository.existsByUserIdAndShopId(userId, shopId)) {
            throw new ResourceNotFoundException("Favourite not found for user ID " + userId + " and shop ID " + shopId);
        }
        favouriteRepository.deleteByUserIdAndShopId(userId, shopId);
    }

    @Override
    public List<Favorite> getFavoritesByUser(UUID userId) {
        return favouriteRepository.findByUserId(userId);
    }

    @Override
    public boolean isFavorite(UUID userId, UUID shopId) {
        return favouriteRepository.existsByUserIdAndShopId(userId, shopId);
    }
}

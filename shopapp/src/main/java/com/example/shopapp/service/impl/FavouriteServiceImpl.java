package com.example.shopapp.service.impl;

import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.model.Favourite;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.User;
import com.example.shopapp.repository.FavouriteRepository;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.repository.UserRepository;
import com.example.shopapp.service.FavouriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavouriteServiceImpl implements FavouriteService {
    private final FavouriteRepository favouriteRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Override
    public Favourite addFavourite(UUID userId, UUID shopId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop with ID " + shopId + " not found"));

        Favourite favourite = new Favourite();
        favourite.setUser(user);
        favourite.setShop(shop);
        return favouriteRepository.save(favourite);
    }

    @Override
    public void removeFavourite(UUID userId, UUID shopId) {
        if (!favouriteRepository.existsByUserIdAndShopId(userId, shopId)) {
            throw new ResourceNotFoundException("Favourite not found for user ID " + userId + " and shop ID " + shopId);
        }
        favouriteRepository.deleteByUserIdAndShopId(userId, shopId);
    }

    @Override
    public List<Favourite> getFavouritesByUser(UUID userId) {
        return favouriteRepository.findByUserId(userId);
    }

    @Override
    public boolean isFavourite(UUID userId, UUID shopId) {
        return favouriteRepository.existsByUserIdAndShopId(userId, shopId);
    }
}

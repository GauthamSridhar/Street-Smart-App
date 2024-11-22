package com.example.shopapp.service.impl;

import com.example.shopapp.dto.response.FavoriteResponseDTO;
import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.mapper.FavoriteMapper;
import com.example.shopapp.model.Favorite;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.User;
import com.example.shopapp.repository.FavouriteRepository;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.repository.UserRepository;
import com.example.shopapp.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {

    private final FavouriteRepository favouriteRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final FavoriteMapper favoriteMapper;

    @Override
    public FavoriteResponseDTO addFavorite(UUID userId, UUID shopId) {
        log.info("Adding Shop ID: {} to User ID: {} favorites", shopId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with ID: " + shopId));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setShop(shop);

        Favorite savedFavorite = favouriteRepository.save(favorite);
        log.info("Shop ID: {} added to User ID: {} favorites", shopId, userId);
        return favoriteMapper.toDTO(savedFavorite);
    }

    @Override
    public void removeFavorite(UUID userId, UUID shopId) {
        log.info("Removing Shop ID: {} from User ID: {} favorites", shopId, userId);

        if (!favouriteRepository.existsByUserIdAndShopId(userId, shopId)) {
            throw new ResourceNotFoundException("Favorite not found for User ID: " + userId + " and Shop ID: " + shopId);
        }

        favouriteRepository.deleteByUserIdAndShopId(userId, shopId);
        log.info("Shop ID: {} removed from User ID: {} favorites", shopId, userId);
    }

    @Override
    public List<FavoriteResponseDTO> getFavoritesByUser(UUID userId) {
        log.info("Fetching favorites for User ID: {}", userId);

        List<Favorite> favorites = favouriteRepository.findByUserId(userId);
        return favorites.stream()
                .map(favoriteMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFavorite(UUID userId, UUID shopId) {
        log.info("Checking if Shop ID: {} is a favorite for User ID: {}", shopId, userId);
        return favouriteRepository.existsByUserIdAndShopId(userId, shopId);
    }
}

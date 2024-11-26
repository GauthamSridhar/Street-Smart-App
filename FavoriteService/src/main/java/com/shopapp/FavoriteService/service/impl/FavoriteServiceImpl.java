package com.shopapp.FavoriteService.service.impl;

import com.shopapp.FavoriteService.dto.favourite.UserDTO;
import com.shopapp.FavoriteService.dto.favourite.response.FavoriteResponseDTO;
import com.shopapp.FavoriteService.exception.ResourceNotFoundException;
import com.shopapp.FavoriteService.dto.favourite.ShopBasicInfoDTO;
import com.shopapp.FavoriteService.feign.ShopFeignClient;
import com.shopapp.FavoriteService.feign.UserFeignClient;
import com.shopapp.FavoriteService.mapper.FavoriteMapper;
import com.shopapp.FavoriteService.model.Favorite;
import com.shopapp.FavoriteService.repository.FavouriteRepository;
import com.shopapp.FavoriteService.service.FavoriteService;
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
    private final UserFeignClient userFeignClient;
    private final ShopFeignClient shopFeignClient;
    private final FavoriteMapper favoriteMapper;

    @Override
    public FavoriteResponseDTO addFavorite(UUID userId, UUID shopId) {
        log.info("Adding Shop ID: {} to User ID: {} favorites", shopId, userId);

        // Fetch the User object via UserService
        UserDTO user = userFeignClient.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        // Validate shop existence via ShopService
        Boolean shopExists = shopFeignClient.doesShopExist(shopId);
        if (shopExists == null || !shopExists) {
            throw new ResourceNotFoundException("Shop not found with ID: " + shopId);
        }

        // Add shopId to user's favorites list if not already present
        if (!user.getFavourites().contains(shopId)) {
            user.getFavourites().add(shopId);

            // Save the updated User object
            userFeignClient.updateUser(userId, user);
        }

        // Optionally, save to FavoriteService's own database if needed
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setShopId(shopId);

        Favorite savedFavorite = favouriteRepository.save(favorite);
        log.info("Shop ID: {} added to User ID: {} favorites", shopId, userId);

        // Fetch shop name
        ShopBasicInfoDTO shopResponse = shopFeignClient.getShopBasicInfo(shopId);

        return favoriteMapper.toDTO(savedFavorite, shopResponse.getName());
    }

    @Override
    public void removeFavorite(UUID userId, UUID shopId) {
        log.info("Removing Shop ID: {} from User ID: {} favorites", shopId, userId);

        // Fetch the User object via UserService
        UserDTO user = userFeignClient.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        // Remove shopId from user's favorites list if present
        if (user.getFavourites().contains(shopId)) {
            user.getFavourites().remove(shopId);

            // Save the updated User object
            userFeignClient.updateUser(userId, user);
        } else {
            throw new ResourceNotFoundException("Favorite not found for User ID: " + userId + " and Shop ID: " + shopId);
        }

        // Optionally, remove from FavoriteService's own database if needed
        favouriteRepository.deleteByUserIdAndShopId(userId, shopId);
        log.info("Shop ID: {} removed from User ID: {} favorites", shopId, userId);
    }

    @Override
    public List<FavoriteResponseDTO> getFavoritesByUser(UUID userId) {
        log.info("Fetching favorites for User ID: {}", userId);

        // Fetch the User object via UserService
        UserDTO user = userFeignClient.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        // Map the user's favorites list to FavoriteResponseDTOs
        return user.getFavourites().stream().map(shopId -> {
            // Fetch shop name
            ShopBasicInfoDTO shopResponse = shopFeignClient.getShopBasicInfo(shopId);
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setShopId(shopId);
            return favoriteMapper.toDTO(favorite, shopResponse.getName());
        }).collect(Collectors.toList());
    }


    @Override
    public boolean isFavorite(UUID userId, UUID shopId) {
        log.info("Checking if Shop ID: {} is a favorite for User ID: {}", shopId, userId);
        return favouriteRepository.existsByUserIdAndShopId(userId, shopId);
    }
}

package com.shopapp.FavoriteService.service.impl;

import com.shopapp.FavoriteService.dto.favourite.UpdateUserRequest;
import com.shopapp.FavoriteService.dto.favourite.UserDTO;
import com.shopapp.FavoriteService.dto.favourite.UserResponse;
import com.shopapp.FavoriteService.dto.favourite.response.FavoriteResponseDTO;
import com.shopapp.FavoriteService.exception.ResourceNotFoundException;
import com.shopapp.FavoriteService.dto.favourite.ShopBasicInfoDTO;
import com.shopapp.FavoriteService.feign.ShopFeignClient;
import com.shopapp.FavoriteService.feign.UserFeignClient;
import com.shopapp.FavoriteService.mapper.FavoriteMapper;
import com.shopapp.FavoriteService.model.Favorite;
import com.shopapp.FavoriteService.repository.FavouriteRepository;
import com.shopapp.FavoriteService.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    @Transactional
    public FavoriteResponseDTO addFavorite(UUID userId, UUID shopId,HttpServletRequest  req) {

        String bearerToken = req.getHeader("Authorization");

        if (bearerToken == null) {
            return null;
        }
        log.info("Adding Shop ID: {} to User ID: {} favorites", shopId, userId);

        // Fetch the User object via UserService
        UserResponse user = userFeignClient.getUser(userId,bearerToken).getBody();
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        UpdateUserRequest request = favoriteMapper.toUpdateDto(user);
        // Validate shop existence via ShopService
        Boolean shopExists = shopFeignClient.doesShopExist(shopId);
        if (shopExists == null || !shopExists) {
            throw new ResourceNotFoundException("Shop not found with ID: " + shopId);
        }
        // Check if shop is already a favorite
        if (request.getFavorites().contains(shopId)) {
            removeFavorite(userId,shopId,req);
            return null;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setShopId(shopId);

        Favorite savedFavorite = favouriteRepository.save(favorite);
        log.info("Shop ID: {} added to User ID: {} favorites", shopId, userId);

        // Add shopId to user's favorites list if not already present
        if (!request.getFavorites().contains(shopId)) {
            request.getFavorites().add(shopId);

            // Save the updated User object
            userFeignClient.updateProfile(userId, request,bearerToken);
            System.out.println("Favorites:" + request.getFavorites());
        }


        // Optionally, save to FavoriteService's own database if needed



        // Fetch shop name
        ShopBasicInfoDTO shopResponse = shopFeignClient.getShopBasicInfo(shopId);

        return favoriteMapper.toDTO(savedFavorite, shopResponse.getName());
    }

    @Override
    @Transactional
    public void removeFavorite(UUID userId, UUID shopId,HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        log.info("Removing Shop ID: {} from User ID: {} favorites", shopId, userId);

        // Fetch the User object via UserService
        UserResponse user = userFeignClient.getUser(userId,bearerToken).getBody();
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        UpdateUserRequest request=favoriteMapper.toUpdateDto(user);

        // Remove shopId from user's favorites list if present
        if (request.getFavorites().contains(shopId)) {
            request.getFavorites().remove(shopId);

            // Save the updated User object
            userFeignClient.updateProfile(userId,request,bearerToken );
        } else {
            throw new ResourceNotFoundException("Favorite not found for User ID: " + userId + " and Shop ID: " + shopId);
        }

        // Optionally, remove from FavoriteService's own database if needed
        favouriteRepository.deleteByUserIdAndShopId(userId, shopId);
        log.info("Shop ID: {} removed from User ID: {} favorites", shopId, userId);
    }

    @Override
    @Transactional
    public List<FavoriteResponseDTO> getFavoritesByUser(UUID userId, HttpServletRequest req) {

        String bearerToken = req.getHeader("Authorization");

        if (bearerToken == null) {
            return null;
        }
        log.info("Fetching favorites for User ID: {}", userId);

        // Fetch the User object via UserService
        UserResponse user = userFeignClient.getUser(userId,bearerToken).getBody();
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        // Map the user's favorites list to FavoriteResponseDTOs
        return user.getFavorites().stream().map(shopId -> {
            // Fetch shop name
            ShopBasicInfoDTO shopResponse = shopFeignClient.getShopBasicInfo(shopId);
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setShopId(shopId);
            return favoriteMapper.toDTO(favorite, shopResponse.getName());
        }).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public boolean isFavorite(UUID userId, UUID shopId) {
        log.info("Checking if Shop ID: {} is a favorite for User ID: {}", shopId, userId);
        return favouriteRepository.existsByUserIdAndShopId(userId, shopId);
    }

    @Override
    public int getFavoriteCount(UUID userId, HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken == null) {
            return 0;
        }
        log.info("Fetching favorite count for User ID: {}", userId);

        // Fetch the User object via UserService
        UserResponse user = userFeignClient.getUser(userId,bearerToken).getBody();
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        return user.getFavorites().size();
    }
}

package com.shopapp.RatingService.service.impl;

import com.shopapp.RatingService.dto.rating.UserDTO;
import com.shopapp.RatingService.dto.rating.request.RatingCreateDTO;
import com.shopapp.RatingService.dto.rating.request.RatingUpdateDTO;
import com.shopapp.RatingService.dto.rating.response.RatingResponseDTO;
import com.shopapp.RatingService.exception.ResourceNotFoundException;
import com.shopapp.RatingService.exception.UnauthorizedActionException;
import com.shopapp.RatingService.feign.ShopFeignClient;
import com.shopapp.RatingService.feign.UserFeignClient;
import com.shopapp.RatingService.mapper.RatingMapper;
import com.shopapp.RatingService.model.Rating;
import com.shopapp.RatingService.repository.RatingRepository;
import com.shopapp.RatingService.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final ShopFeignClient shopFeignClient;
    private final UserFeignClient userFeignClient;
    private final RatingMapper ratingMapper;

    @Override
    @Transactional
    public RatingResponseDTO addRating(UUID userId, UUID shopId, RatingCreateDTO ratingDTO) {
        log.info("User ID: {} adding rating for Shop ID: {}", userId, shopId);

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

        Rating rating = ratingMapper.toEntity(ratingDTO);
        rating.setUserId(userId);
        rating.setShopId(shopId);

        Rating savedRating = ratingRepository.save(rating);
        log.info("Rating added successfully for Shop ID: {} by User ID: {}", shopId, userId);

        // Add rating ID to user's ratings list
        user.getRatings().add(savedRating.getId());

        // Update the user via UserService
        userFeignClient.updateUser(userId, user);

        return ratingMapper.toDTO(savedRating);
    }

    @Override
    @Transactional
    public RatingResponseDTO updateRating(UUID userId, UUID ratingId, RatingUpdateDTO ratingDTO) {
        log.info("User ID: {} updating Rating ID: {}", userId, ratingId);

        Rating existingRating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with ID: " + ratingId));

        if (!existingRating.getUserId().equals(userId)) {
            throw new UnauthorizedActionException("User ID: " + userId + " is not authorized to update this rating");
        }

        ratingMapper.updateEntity(existingRating, ratingDTO);
        Rating updatedRating = ratingRepository.save(existingRating);
        log.info("Rating ID: {} updated successfully", ratingId);
        return ratingMapper.toDTO(updatedRating);
    }

    @Override
    @Transactional
    public void deleteRating(UUID userId, UUID ratingId) {
        log.info("User ID: {} deleting Rating ID: {}", userId, ratingId);

        Rating existingRating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with ID: " + ratingId));

        if (!existingRating.getUserId().equals(userId)) {
            throw new UnauthorizedActionException("User ID: " + userId + " is not authorized to delete this rating");
        }

        ratingRepository.delete(existingRating);
        log.info("Rating ID: {} deleted successfully", ratingId);

        // Fetch the User object via UserService
        UserDTO user = userFeignClient.getUserById(userId);
        if (user != null) {
            // Remove rating ID from user's ratings list
            user.getRatings().remove(ratingId);

            // Update the user via UserService
            userFeignClient.updateUser(userId, user);
        }
    }

    @Override
    public List<RatingResponseDTO> getShopRatings(UUID shopId) {
        log.info("Fetching ratings for Shop ID: {}", shopId);

        List<Rating> ratings = ratingRepository.findByShopId(shopId);
        return ratings.stream().map(ratingMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public RatingResponseDTO getRating(UUID ratingId) {
        log.info("Fetching Rating ID: {}", ratingId);

        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with ID: " + ratingId));
        return ratingMapper.toDTO(rating);
    }
}
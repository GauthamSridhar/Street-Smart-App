package com.example.shopapp.service.impl;

import com.example.shopapp.dto.request.RatingCreateDTO;
import com.example.shopapp.dto.request.RatingUpdateDTO;
import com.example.shopapp.dto.response.RatingResponseDTO;
import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.exception.UnauthorizedActionException;
import com.example.shopapp.mapper.RatingMapper;
import com.example.shopapp.model.Rating;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.User;
import com.example.shopapp.repository.RatingRepository;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.repository.UserRepository;
import com.example.shopapp.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final RatingMapper ratingMapper;

    @Override
    @Transactional
    public RatingResponseDTO addRating(UUID userId, UUID shopId, RatingCreateDTO ratingDTO) {
        log.info("User ID: {} adding rating for Shop ID: {}", userId, shopId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with ID: " + shopId));

        Rating rating = ratingMapper.toEntity(ratingDTO);
        rating.setUser(user);
        rating.setShop(shop);

        Rating savedRating = ratingRepository.save(rating);
        log.info("Rating added successfully for Shop ID: {} by User ID: {}", shopId, userId);
        return ratingMapper.toDTO(savedRating);
    }

    @Override
    @Transactional
    public RatingResponseDTO updateRating(UUID userId, UUID ratingId, RatingUpdateDTO ratingDTO) {
        log.info("User ID: {} updating Rating ID: {}", userId, ratingId);

        Rating existingRating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with ID: " + ratingId));

        if (!existingRating.getUser().getId().equals(userId)) {
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

        if (!existingRating.getUser().getId().equals(userId)) {
            throw new UnauthorizedActionException("User ID: " + userId + " is not authorized to delete this rating");
        }

        ratingRepository.delete(existingRating);
        log.info("Rating ID: {} deleted successfully", ratingId);
    }

    @Override
    public List<RatingResponseDTO> getShopRatings(UUID shopId) {
        log.info("Fetching ratings for Shop ID: {}", shopId);

        List<Rating> ratings = ratingRepository.findByShopId(shopId);
        return ratings.stream().map(ratingMapper::toDTO).toList();
    }

    @Override
    public RatingResponseDTO getRating(UUID ratingId) {
        log.info("Fetching Rating ID: {}", ratingId);

        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with ID: " + ratingId));
        return ratingMapper.toDTO(rating);
    }
}

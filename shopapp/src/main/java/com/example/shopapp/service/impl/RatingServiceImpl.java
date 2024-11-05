package com.example.shopapp.service.impl;

import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.exception.UnauthorizedActionException;
import com.example.shopapp.model.Rating;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.User;
import com.example.shopapp.repository.RatingRepository;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.repository.UserRepository;
import com.example.shopapp.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Rating addRating(UUID userId, UUID shopId, Rating request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop with ID " + shopId + " not found"));

        Rating rating = new Rating();
        rating.setUser(user);
        rating.setShop(shop);
        rating.setRating(request.getRating());
        rating.setReview(request.getReview());

        return ratingRepository.save(rating);
    }

    @Override
    @Transactional
    public Rating updateRating(UUID userId, UUID ratingId, Rating request) {
        Rating existingRating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating with ID " + ratingId + " not found"));

        if (!existingRating.getUser().getId().equals(userId)) {
            throw new UnauthorizedActionException("User with ID " + userId + " is not authorized to update this rating");
        }

        existingRating.setRating(request.getRating());
        existingRating.setReview(request.getReview());
        return ratingRepository.save(existingRating);
    }

    @Override
    @Transactional
    public void deleteRating(UUID userId, UUID ratingId) {
        Rating existingRating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating with ID " + ratingId + " not found"));

        if (!existingRating.getUser().getId().equals(userId)) {
            throw new UnauthorizedActionException("User with ID " + userId + " is not authorized to delete this rating");
        }

        ratingRepository.delete(existingRating);
    }

    @Override
    public List<Rating> getShopRatings(UUID shopId) {
        return ratingRepository.findByShopId(shopId);
    }

    @Override
    public Rating getRating(UUID ratingId) {
        return ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating with ID " + ratingId + " not found"));
    }
}

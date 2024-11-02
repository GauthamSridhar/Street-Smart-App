package com.example.shopapp.service.impl;

import com.example.shopapp.exception.ResourceNotFoundException;
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Retrieve the shop using the provided shopId
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        Rating rating = new Rating();
        rating.setUser(user);
        rating.setShop(shop); // Use the retrieved shop directly
        rating.setRating(request.getRating());
        rating.setReview(request.getReview());

        return ratingRepository.save(rating);
    }


    @Override
    @Transactional
    public Rating updateRating(UUID userId, UUID ratingId, Rating request) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));

        if (!rating.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to update this rating");
        }

        rating.setRating(request.getRating());
        rating.setReview(request.getReview());
        return ratingRepository.save(rating);
    }

    @Override
    @Transactional
    public void deleteRating(UUID userId, UUID ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));

        if (!rating.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this rating");
        }

        ratingRepository.delete(rating);
    }

    @Override
    public List<Rating> getShopRatings(UUID shopId) {
        return ratingRepository.findByShopId(shopId);
    }

    @Override
    public Rating getRating(UUID ratingId) {
        return ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));
    }
}

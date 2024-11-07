package com.example.shopapp.service.impl;

import com.example.shopapp.dto.RatingCreateDTO;
import com.example.shopapp.dto.RatingUpdateDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final RatingMapper ratingMapper;

    @Override
    @Transactional
    public RatingResponseDTO addRating(UUID userId, UUID shopId, RatingCreateDTO ratingDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop with ID " + shopId + " not found"));

        Rating rating = ratingMapper.toEntity(ratingDTO);
        rating.setUser(user);
        rating.setShop(shop);

        Rating savedRating = ratingRepository.save(rating);
        return ratingMapper.toDTO(savedRating);
    }

    @Override
    @Transactional
    public RatingResponseDTO updateRating(UUID userId, UUID ratingId, RatingUpdateDTO ratingDTO) {
        Rating existingRating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating with ID " + ratingId + " not found"));

        if (!existingRating.getUser().getId().equals(userId)) {
            throw new UnauthorizedActionException("User with ID " + userId + " is not authorized to update this rating");
        }

        ratingMapper.updateEntity(existingRating, ratingDTO);
        Rating updatedRating = ratingRepository.save(existingRating);
        return ratingMapper.toDTO(updatedRating);
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
    public List<RatingResponseDTO> getShopRatings(UUID shopId) {
        return ratingRepository.findByShopId(shopId).stream()
                .map(ratingMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RatingResponseDTO getRating(UUID ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating with ID " + ratingId + " not found"));
        return ratingMapper.toDTO(rating);
    }
}

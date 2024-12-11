package com.shopapp.RatingService.service.impl;

import com.shopapp.RatingService.dto.rating.ShopResponse;
import com.shopapp.RatingService.dto.rating.UpdateShopRequest;
import com.shopapp.RatingService.dto.rating.UpdateUserRequest;
import com.shopapp.RatingService.dto.rating.UserResponse;
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
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final ShopFeignClient shopFeignClient;
    private final UserFeignClient userFeignClient;
    private final RatingMapper ratingMapper;

    @Override
    @Transactional
    public RatingResponseDTO addRating(UUID userId, UUID shopId, RatingCreateDTO ratingDTO, HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");

        if (bearerToken == null) {
            return null;
        }
        log.info("User ID: {} adding rating for Shop ID: {}", userId, shopId);

        // Fetch the User object via UserService
        UserResponse user = userFeignClient.getUser(userId,bearerToken).getBody();
        System.out.println("User found");
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        UpdateUserRequest request1=ratingMapper.toUpdateDto(user);

        // Validate shop existence via ShopService
        ShopResponse shopExists = shopFeignClient.getShop(shopId,bearerToken).getBody();
        System.out.println("Shop :" + shopExists);
        if (shopExists == null ) {
            throw new ResourceNotFoundException("Shop not found with ID: " + shopId);
        }

        UpdateShopRequest shopRequest=ratingMapper.toUpdateShop(shopExists);

        Rating rating = ratingMapper.toEntity(ratingDTO);
        rating.setUserId(userId);
        rating.setShopId(shopId);

        Rating savedRating = ratingRepository.save(rating);
        log.info("Rating added successfully for Shop ID: {} by User ID: {}", shopId, userId);

        // Add rating ID to user's ratings list
        if(request1.getRatings()==null){
            request1.setRatings(new ArrayList<>());
        }
        request1.getRatings().add(savedRating.getId());
        userFeignClient.updateProfile(userId, request1,bearerToken);
        if(shopRequest.getRatings()==null){
            shopRequest.setRatings(new ArrayList<>());
        }
        shopRequest.getRatings().add(savedRating.getId());
        shopFeignClient.updateShop(shopId,shopRequest);


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
    public void deleteRating(UUID userId, UUID ratingId,UUID shopId,HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");


        log.info("User ID: {} deleting Rating ID: {}", userId, ratingId);

        Rating existingRating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with ID: " + ratingId));

        if (!existingRating.getUserId().equals(userId)) {
            throw new UnauthorizedActionException("User ID: " + userId + " is not authorized to delete this rating");
        }

        ratingRepository.delete(existingRating);
        log.info("Rating ID: {} deleted successfully", ratingId);

        // Fetch the User object via UserService
        UserResponse user = userFeignClient.getUser(userId,bearerToken).getBody();
        assert user != null;
        UpdateUserRequest request1=ratingMapper.toUpdateDto(user);
        ShopResponse shop=shopFeignClient.getShop(shopId,bearerToken).getBody();
        assert shop != null;
        UpdateShopRequest request2=ratingMapper.toUpdateShop(shop);
        if (request1 != null) {
            // Remove rating ID from user's ratings list
            request1.getRatings().remove(ratingId);


            // Update the user via UserService
            userFeignClient.updateProfile(userId, request1,bearerToken);
        }
        if (request2 != null) {
            // Remove rating ID from user's ratings list
            request2.getRatings().remove(ratingId);


            // Update the user via UserService
            shopFeignClient.updateShop(shopId, request2);
        }
    }

    @Override
    @Transactional
    public List<RatingResponseDTO> getShopRatings(UUID shopId) {
        log.info("Fetching ratings for Shop ID: {}", shopId);

        List<Rating> ratings = ratingRepository.findByShopId(shopId);
        return ratings.stream().map(ratingMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RatingResponseDTO getRating(UUID ratingId) {
        log.info("Fetching Rating ID: {}", ratingId);

        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with ID: " + ratingId));
        return ratingMapper.toDTO(rating);
    }

    @Override
    public Long getShopRatingsCount(UUID shopId) {
        log.info("Fetching ratings count for Shop ID: {}", shopId);

        return ratingRepository.countByShopId(shopId);
    }
}

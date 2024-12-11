package com.shopapp.RatingService.mapper;


import com.shopapp.RatingService.dto.rating.ShopResponse;
import com.shopapp.RatingService.dto.rating.UpdateShopRequest;
import com.shopapp.RatingService.dto.rating.UpdateUserRequest;
import com.shopapp.RatingService.dto.rating.UserResponse;
import com.shopapp.RatingService.dto.rating.request.RatingCreateDTO;
import com.shopapp.RatingService.dto.rating.request.RatingUpdateDTO;
import com.shopapp.RatingService.dto.rating.response.RatingResponseDTO;
import com.shopapp.RatingService.model.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {

    public Rating toEntity(RatingCreateDTO dto) {
        Rating rating = new Rating();
        rating.setRating(dto.getRating());
        rating.setReview(dto.getReview());
        return rating;
    }

    public void updateEntity(Rating rating, RatingUpdateDTO dto) {
        rating.setRating(dto.getRating());
        rating.setReview(dto.getReview());
    }
    public UpdateUserRequest toUpdateDto(UserResponse user) {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail(user.getEmail());
        request.setPassword(request.getPassword());
        request.setPhoneNumber(user.getPhoneNumber());
        request.setFullName(user.getFullName());
        request.setRatings(user.getRatings());
        request.setFavorites(user.getFavorites());
        return request;
    }
    public UpdateShopRequest toUpdateShop(ShopResponse shop){
        UpdateShopRequest request=new UpdateShopRequest();
        request.setName(shop.getName());
        request.setDescription(shop.getDescription());
        request.setAddress(shop.getAddress());
        request.setLatitude(shop.getLatitude());
        request.setLongitude(shop.getLongitude());
        request.setOwnerId(shop.getOwnerId());
        request.setProducts(shop.getProducts());
        request.setImages(shop.getImages());
        request.setRatings(shop.getRatings());
        return request;
    }

    public RatingResponseDTO toDTO(Rating rating) { // Updated to return RatingResponseDTO
        RatingResponseDTO dto = new RatingResponseDTO();
        dto.setId(rating.getId());
        dto.setRating(rating.getRating());
        dto.setReview(rating.getReview());
        dto.setUserId(rating.getUserId().toString());
        dto.setShopId(rating.getShopId().toString());
        dto.setUpdatedAt(rating.getUpdatedAt().toString());

        return dto;
    }
}

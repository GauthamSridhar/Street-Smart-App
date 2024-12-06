package com.shopapp.FavoriteService.mapper;

import com.shopapp.FavoriteService.dto.favourite.UpdateUserRequest;
import com.shopapp.FavoriteService.dto.favourite.UserResponse;
import com.shopapp.FavoriteService.dto.favourite.response.FavoriteResponseDTO;
import com.shopapp.FavoriteService.model.Favorite;
import org.springframework.stereotype.Component;

@Component
public class FavoriteMapper {

    public FavoriteResponseDTO toDTO(Favorite favorite, String shopName) {
        if (favorite == null) {
            return null;
        }

        FavoriteResponseDTO dto = new FavoriteResponseDTO();
        dto.setId(favorite.getId());
        dto.setShopId(favorite.getShopId());
        dto.setShopName(shopName);
        dto.setUserId(favorite.getUserId());
        return dto;
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
}

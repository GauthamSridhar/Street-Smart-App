package com.shopapp.FavoriteService.mapper;

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
}

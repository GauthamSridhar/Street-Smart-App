package com.shopapp.FavoriteService.mapper;

import com.shopapp.FavoriteService.dto.favourite.response.FavoriteResponseDTO;
import com.shopapp.FavoriteService.model.Favorite;
import org.springframework.stereotype.Component;

@Component
public class FavoriteMapper {
  public FavoriteResponseDTO toDTO(Favorite favorite, String shopName) {
    var dto = new FavoriteResponseDTO();
    dto.setId(favorite.getId());
    dto.setUserId(favorite.getUserId());
    dto.setShopId(favorite.getShopId());
    dto.setShopName(shopName);
    return dto;
  }
}

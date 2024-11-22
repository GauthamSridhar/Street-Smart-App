package com.example.shopapp.mapper;

import com.example.shopapp.dto.response.FavoriteResponseDTO;
import com.example.shopapp.model.Favorite;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FavoriteMapper {

    public FavoriteResponseDTO toDTO(Favorite favorite) {
        if (favorite == null) {
            return null;
        }

        FavoriteResponseDTO dto = new FavoriteResponseDTO();
//        dto.setId(favorite.getId());
//        dto.setUserId(favorite.getUser().getId());
//        dto.setShopId(favorite.getShop().getId());
        return dto;
    }

    public List<FavoriteResponseDTO> toDTOList(List<Favorite> favorites) {
        if (favorites == null) {
            return null;
        }

        return favorites.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}

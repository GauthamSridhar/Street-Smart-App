package com.example.shopapp.mapper;

import com.example.shopapp.dto.request.ShopRegistrationRequest;
import com.example.shopapp.dto.response.*;
import com.example.shopapp.model.Rating;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper class for converting between Shop entities and DTOs.
 */
@Component
@RequiredArgsConstructor
public class ShopMapper {

    private final UserMapper userMapper;
    private final ImageMapper imageMapper;
    private final ProductMapper productMapper;
    private final RatingMapper ratingMapper;

    /**
     * Converts a ShopRegistrationRequest DTO to a Shop entity.
     *
     * @param request the shop registration request DTO
     * @return the Shop entity
     */
    public Shop toEntity(@Valid ShopRegistrationRequest request) {
        Shop shop = new Shop();
        shop.setName(request.getName());
        shop.setDescription(request.getDescription());
        shop.setAddress(request.getAddress());
        shop.setLatitude(request.getLatitude());
        shop.setLongitude(request.getLongitude());
        shop.setStatus(ShopStatus.PENDING);
        return shop;
    }

    /**
     * Converts a Shop entity to a ShopResponse DTO.
     *
     * @param shop the Shop entity
     * @return the ShopResponse DTO
     */
    public ShopResponse toResponse(Shop shop) {
        ShopResponse response = new ShopResponse();
        response.setId(shop.getId());
        response.setName(shop.getName());
        response.setDescription(shop.getDescription());
        response.setAddress(shop.getAddress());
        response.setLatitude(shop.getLatitude());
        response.setLongitude(shop.getLongitude());
        response.setStatus(shop.getStatus());
        response.setOwner(userMapper.toResponse(shop.getOwner()));
        response.setAverageRating(calculateAverageRating(shop));
        response.setImages(shop.getImages().stream()
                .map(imageMapper::toDTO)
                .collect(Collectors.toList()));
        return response;
    }

    /**
     * Converts a Shop entity to a ShopDetailResponse DTO.
     *
     * @param shop the Shop entity
     * @return the ShopDetailResponse DTO
     */
    public ShopDetailResponse toDetailResponse(Shop shop) {
        ShopDetailResponse response = new ShopDetailResponse();
        response.setId(shop.getId());

        response.setProducts(shop.getProducts().stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList()));
        response.setRatings(shop.getRatings().stream()
                .map(ratingMapper::toDTO)
                .collect(Collectors.toList()));
        return response;
    }

    /**
     * Converts a Shop entity to a ShopSummaryResponse DTO.
     *
     * @param shop the Shop entity
     * @return the ShopSummaryResponse DTO
     */
    public ShopSummaryResponse toSummaryResponse(Shop shop) {
        ShopSummaryResponse response = new ShopSummaryResponse();
        response.setId(shop.getId());
        response.setName(shop.getName());
        response.setDescription(shop.getDescription());
        response.setAddress(shop.getAddress());
        response.setStatus(shop.getStatus());
        response.setAverageRating(calculateAverageRating(shop));
        response.setPrimaryImage(shop.getImages().stream()
                .findFirst()
                .map(imageMapper::toDTO)
                .orElse(null));
        return response;
    }

    /**
     * Calculates the average rating of a shop.
     *
     * @param shop the Shop entity
     * @return the average rating as a Double
     */
    private Double calculateAverageRating(Shop shop) {
        if (shop.getRatings() == null || shop.getRatings().isEmpty()) {
            return 0.0;
        }
        return shop.getRatings().stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);
    }
}

package com.example.shopapp.mapper;

import com.example.shopapp.dto.request.ShopRegistrationRequest;
import com.example.shopapp.dto.response.ShopDetailResponse;
import com.example.shopapp.dto.response.ShopResponse;
import com.example.shopapp.dto.response.ShopSummaryResponse;
import com.example.shopapp.model.Rating;
import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopMapper {
    private final UserMapper userMapper;
    private final ImageMapper imageMapper;
    private final ProductMapper productMapper;
    private final RatingMapper ratingMapper;

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
                .toList());
        return response;
    }

    public ShopDetailResponse toDetailResponse(Shop shop) {
        ShopDetailResponse response = new ShopDetailResponse();
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
                .toList());
        response.setProducts(shop.getProducts().stream()
                .map(productMapper::toDTO)
                .toList());
        response.setRatings(shop.getRatings().stream()
                .map(ratingMapper::toDTO)  // Updated this line
                .toList());
        return response;
    }

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

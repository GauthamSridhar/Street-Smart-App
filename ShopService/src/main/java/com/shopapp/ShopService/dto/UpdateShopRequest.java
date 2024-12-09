package com.shopapp.ShopService.dto;

import com.shopapp.ShopService.dto.image.response.ImageResponseDTO;
import com.shopapp.ShopService.dto.product.response.ProductResponseDTO;
import com.shopapp.ShopService.model.ShopStatus;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateShopRequest {
    private String name;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private ShopStatus status;
    private UUID ownerId;
    private List<ProductResponseDTO> products;
    private List<ImageResponseDTO> images;
    private List<UUID> ratings;
}

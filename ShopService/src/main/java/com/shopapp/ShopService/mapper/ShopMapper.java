package com.shopapp.ShopService.mapper;

import com.shopapp.ShopService.dto.ShopBasicInfoDTO;
import com.shopapp.ShopService.dto.UpdateShopRequest;
import com.shopapp.ShopService.dto.image.response.ImageResponseDTO;
import com.shopapp.ShopService.dto.product.response.ProductResponseDTO;
import com.shopapp.ShopService.dto.shop.request.ShopRegistrationRequest;
import com.shopapp.ShopService.dto.shop.response.ShopResponse;
import com.shopapp.ShopService.model.Shop;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ShopMapper {

    public Shop toEntity(ShopRegistrationRequest request) {
        Shop shop = new Shop();
        shop.setName(request.getName());
        shop.setDescription(request.getDescription());
        shop.setAddress(request.getAddress());
        shop.setLatitude(request.getLatitude());
        shop.setLongitude(request.getLongitude());
        shop.setCategory(request.getCategory());
        return shop;
    }

    public void updateEntity(Shop shop, UpdateShopRequest request) {
        shop.setName(request.getName());
        shop.setDescription(request.getDescription());
        shop.setAddress(request.getAddress());
        shop.setLatitude(request.getLatitude());
        shop.setLongitude(request.getLongitude());
        shop.setOwnerId(request.getOwnerId());
        shop.setRatings(request.getRatings());
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
        response.setCategory(shop.getCategory());
        response.setOwnerId(shop.getOwnerId());
        response.setProducts(shop.getProducts().stream()
                .map(product -> {
                    ProductResponseDTO productResponse = new ProductResponseDTO();
                    productResponse.setId(product.getId());
                    productResponse.setName(product.getName());
                    productResponse.setAvailable(product.isAvailable());
                    return productResponse;
                })
                .collect(Collectors.toList()));
        response.setImages(shop.getImages().stream()
                .map(image -> {
                    ImageResponseDTO imageResponse = new ImageResponseDTO();
                    imageResponse.setId(image.getId());
                    imageResponse.setFileName(image.getFileName());
                    imageResponse.setFileType(image.getFileType());
                    imageResponse.setShopId(image.getShop().getId());
                    imageResponse.setFileSizeInBytes(image.getImageData().length);
                    return imageResponse;
                })
                .collect(Collectors.toList()));
        response.setRatings(shop.getRatings());
        return response;
    }

    public ShopBasicInfoDTO toBasicInfo(Shop shop) {
        ShopBasicInfoDTO basicInfo = new ShopBasicInfoDTO();
        basicInfo.setId(shop.getId());
        basicInfo.setName(shop.getName());
        return basicInfo;
    }
}

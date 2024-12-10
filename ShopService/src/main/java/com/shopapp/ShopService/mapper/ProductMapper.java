package com.shopapp.ShopService.mapper;

import com.shopapp.ShopService.dto.product.request.AddProductRequest;
import com.shopapp.ShopService.dto.product.response.ProductResponseDTO;
import com.shopapp.ShopService.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(AddProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setAvailable(request.isAvailable());
        return product;
    }

    public ProductResponseDTO toDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setAvailable(product.isAvailable());
        dto.setShopId(product.getShop().getId());
        return dto;
    }

    public void updateEntity(Product existingProduct, AddProductRequest request) {
        // Only update name if provided and non-empty
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            existingProduct.setName(request.getName());
        }

        // Always update availability if needed
        existingProduct.setAvailable(request.isAvailable());
    }

}

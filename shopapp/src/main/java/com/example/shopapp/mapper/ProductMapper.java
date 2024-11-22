package com.example.shopapp.mapper;

import com.example.shopapp.dto.request.AddProductRequest;
import com.example.shopapp.dto.response.ProductResponseDTO;
import com.example.shopapp.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(AddProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setAvailable(request.isAvailable());
        return product;
    }

    public ProductResponseDTO toDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setAvailable(product.isAvailable());
        dto.setShopId(product.getShop().getId());
        return dto;
    }

    public void updateEntity(Product existingProduct, AddProductRequest request) {
        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setAvailable(request.isAvailable());
    }
}

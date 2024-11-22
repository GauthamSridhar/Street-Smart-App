package com.example.shopapp.service;

import com.example.shopapp.dto.request.AddProductRequest;
import com.example.shopapp.dto.response.ProductResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductResponseDTO addProduct(UUID shopId, AddProductRequest request);
    ProductResponseDTO updateProduct(UUID productId, AddProductRequest request);
    void deleteProduct(UUID productId);
    List<ProductResponseDTO> getProductsByShop(UUID shopId);
    ProductResponseDTO getProductById(UUID productId);
}

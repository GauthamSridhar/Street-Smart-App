package com.shopapp.ShopService.service;



import com.shopapp.ShopService.dto.product.request.AddProductRequest;
import com.shopapp.ShopService.dto.product.response.ProductResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductResponseDTO addProduct(UUID shopId, AddProductRequest request);
    ProductResponseDTO updateProduct(UUID productId, AddProductRequest request);
    void deleteProduct(UUID productId);
    List<ProductResponseDTO> getProductsByShop(UUID shopId);
    ProductResponseDTO getProductById(UUID productId);

    Long getProductCount(UUID shopId);
}

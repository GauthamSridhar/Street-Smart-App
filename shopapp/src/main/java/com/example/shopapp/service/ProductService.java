package com.example.shopapp.service;

import com.example.shopapp.model.Product;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    Product addProduct(UUID shopId, Product product);
    Product updateProduct(UUID productId, Product product);
    void deleteProduct(UUID productId);
    List<Product> getProductsByShop(UUID shopId);
    List<Product> getAvailableProductsByShop(UUID shopId);
    Product getProductById(UUID productId);
}

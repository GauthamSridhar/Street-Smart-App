package com.example.shopapp.service.impl;

import com.example.shopapp.model.Product;
import com.example.shopapp.model.Shop;
import com.example.shopapp.repository.ProductRepository;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    @Override
    public Product addProduct(UUID shopId, Product product) {
        // Fetch the shop
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        product.setShop(shop); // Set the shop reference

        // Save the product to the repository
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(UUID productId, Product product) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Update the product fields
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setAvailable(product.isAvailable());

        // Save the updated product
        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(productId);
    }

    @Override
    public List<Product> getProductsByShop(UUID shopId) {
        return productRepository.findByShopId(shopId);
    }

    @Override
    public List<Product> getAvailableProductsByShop(UUID shopId) {
        return productRepository.findByShopIdAndAvailable(shopId, true);
    }

    @Override
    public Product getProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}

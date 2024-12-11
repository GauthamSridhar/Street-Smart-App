package com.shopapp.ShopService.service.impl;

import com.shopapp.ShopService.dto.product.request.AddProductRequest;
import com.shopapp.ShopService.dto.product.response.ProductResponseDTO;
import com.shopapp.ShopService.exception.ResourceNotFoundException;
import com.shopapp.ShopService.mapper.ProductMapper;
import com.shopapp.ShopService.model.Product;
import com.shopapp.ShopService.model.Shop;
import com.shopapp.ShopService.repository.ProductRepository;
import com.shopapp.ShopService.repository.ShopRepository;
import com.shopapp.ShopService.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponseDTO addProduct(UUID shopId, AddProductRequest request) {
        log.info("Adding product to Shop ID: {}", shopId);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with ID: " + shopId));

        Product product = productMapper.toEntity(request);
        product.setShop(shop);

        Product savedProduct = productRepository.save(product);
        log.info("Product added successfully to Shop ID: {}", shopId);

        return productMapper.toDTO(savedProduct);
    }

    @Override
    public ProductResponseDTO updateProduct(UUID productId, AddProductRequest request) {
        log.info("Updating Product ID: {}", productId);

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        // Just toggle availability, do not update name from request
        existingProduct.setAvailable(!existingProduct.isAvailable());

        // Do not call productMapper.updateEntity(existingProduct, request) if not changing name
        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product ID: {} updated successfully", productId);

        return productMapper.toDTO(updatedProduct);
    }


    @Override
    public void deleteProduct(UUID productId) {
        log.info("Deleting Product ID: {}", productId);

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with ID: " + productId);
        }

        productRepository.deleteById(productId);
        log.info("Product ID: {} deleted successfully", productId);
    }

    @Override
    public List<ProductResponseDTO> getProductsByShop(UUID shopId) {
        log.info("Fetching products for Shop ID: {}", shopId);

        List<Product> products = productRepository.findByShopId(shopId);
        return products.stream()
                .map(productMapper::toDTO)
                .toList();
    }

    @Override
    public ProductResponseDTO getProductById(UUID productId) {
        log.info("Fetching Product ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        return productMapper.toDTO(product);
    }

    @Override
    public Long getProductCount(UUID shopId) {
        log.info("Fetching product count for Shop ID: {}", shopId);

        return productRepository.countByShopId(shopId);
    }
}

package com.shopapp.ShopService.controller;

import com.shopapp.ShopService.dto.product.request.AddProductRequest;
import com.shopapp.ShopService.dto.product.response.ProductResponseDTO;
import com.shopapp.ShopService.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for product-related operations.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Adds a product to a shop's inventory.
     *
     * @param shopId         the ID of the shop
     * @param productRequest the product addition request DTO
     * @return the added product response DTO
     */
    @PostMapping
    public ResponseEntity<ProductResponseDTO> addProduct(
            @RequestParam UUID shopId,
            @Valid @RequestBody AddProductRequest productRequest) {
        log.info("Adding product to Shop ID: {}", shopId);
        ProductResponseDTO addedProduct = productService.addProduct(shopId, productRequest);
        return ResponseEntity.ok(addedProduct);
    }

    /**
     * Updates an existing product.
     *
     * @param productId      the ID of the product to update
     * @param productRequest the product update request DTO
     * @return the updated product response DTO
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody AddProductRequest productRequest) {
        log.info("Updating Product ID: {}", productId);
        ProductResponseDTO updatedProduct = productService.updateProduct(productId, productRequest);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Deletes a product.
     *
     * @param productId the ID of the product to delete
     * @return a 204 No Content response
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID productId) {
        log.info("Deleting Product ID: {}", productId);
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Fetches products for a specific shop.
     *
     * @param shopId the ID of the shop
     * @return a list of products for the shop
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getProductsByShop(@RequestParam UUID shopId) {
        log.info("Fetching products for Shop ID: {}", shopId);
        List<ProductResponseDTO> products = productService.getProductsByShop(shopId);
        return ResponseEntity.ok(products);
    }

    /*get count of products of a particular shop*/
    @GetMapping("/count/{shopId}")
    public ResponseEntity<Long> getProductCount(@PathVariable UUID shopId) {
        log.info("Fetching product count for Shop ID: {}", shopId);
        Long count = productService.getProductCount(shopId);
        return ResponseEntity.ok(count);
    }
}

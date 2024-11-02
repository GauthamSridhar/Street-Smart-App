package com.example.shopapp.controller;

import com.example.shopapp.model.Product;
import com.example.shopapp.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Product> addProduct(
            @RequestParam UUID shopId, // Changed to @RequestParam for explicit passing
            @Valid @RequestBody Product product) {
        Product addedProduct = productService.addProduct(shopId, product);
        return ResponseEntity.ok(addedProduct);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(productId, product);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable UUID productId) {
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getProductsByShop(@RequestParam UUID shopId) { // Changed to @RequestParam for explicit passing
        List<Product> products = productService.getProductsByShop(shopId);
        return ResponseEntity.ok(products);
    }
}

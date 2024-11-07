package com.example.shopapp.controller;

import com.example.shopapp.dto.request.AddProductRequest;
import com.example.shopapp.dto.response.ProductResponseDTO;
import com.example.shopapp.mapper.ProductMapper;
import com.example.shopapp.model.Product;
import com.example.shopapp.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> addProduct(
            @RequestParam UUID shopId,
            @Valid @RequestBody AddProductRequest productRequest) {  // Change to AddProductRequest
        Product product = productMapper.toEntity(productRequest); // Ensure correct mapping
        Product addedProduct = productService.addProduct(shopId, product);
        return ResponseEntity.ok(productMapper.toDTO(addedProduct));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody AddProductRequest productRequest) {  // Change to AddProductRequest
        Product product = productMapper.toEntity(productRequest); // Ensure correct mapping
        Product updatedProduct = productService.updateProduct(productId, product);
        return ResponseEntity.ok(productMapper.toDTO(updatedProduct));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable UUID productId) {
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(productMapper.toDTO(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getProductsByShop(@RequestParam UUID shopId) {
        List<Product> products = productService.getProductsByShop(shopId);
        List<ProductResponseDTO> responseDTOs = products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }
}

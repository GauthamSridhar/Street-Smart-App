package com.shopapp.ShopService.controller;

import com.shopapp.ShopService.dto.product.response.ProductSearchResult;
import com.shopapp.ShopService.service.ProductSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProductSearchController {
  private final ProductSearch search;

  @GetMapping("/api/products/search")
  public Page<ProductSearchResult> search(
      @RequestParam(defaultValue = "") String q,
      @RequestParam(defaultValue = "") String category,
      @RequestParam(defaultValue = "true") boolean availableOnly,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) java.math.BigDecimal minPrice,
      @RequestParam(required = false) java.math.BigDecimal maxPrice,
      @RequestParam(defaultValue = "") String currency,
      @RequestParam(required = false) Double latitude,
      @RequestParam(required = false) Double longitude,
      @RequestParam(required = false) Double radiusKm,
      @RequestParam(defaultValue = "false") boolean fuzzy) {
    return search.search(
        q,
        category,
        availableOnly,
        page,
        size,
        minPrice,
        maxPrice,
        currency,
        latitude,
        longitude,
        radiusKm,
        fuzzy);
  }
}

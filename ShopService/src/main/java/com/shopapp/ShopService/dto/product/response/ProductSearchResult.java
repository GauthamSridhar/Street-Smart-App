package com.shopapp.ShopService.dto.product.response;

import com.shopapp.ShopService.model.ShopStatus;
import java.util.UUID;

/** A catalogue match, not a shop-name match or an entire shop aggregate. */
public record ProductSearchResult(
    UUID id,
    String name,
    boolean available,
    UUID shopId,
    String shopName,
    String category,
    String address,
    Double latitude,
    Double longitude,
    ShopStatus shopStatus,
    java.math.BigDecimal price,
    String currency,
    java.time.Instant updatedAt) {}

// repository/ProductRepository.java
package com.shopapp.ShopService.repository;

import com.shopapp.ShopService.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByShopId(UUID shopId);
    List<Product> findByShopIdAndAvailable(UUID shopId, boolean available);
    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.id = :shopId")
    Long countByShopId(UUID shopId);
}

// repository/ProductRepository.java
package com.example.shopapp.repository;

import com.example.shopapp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByShopId(UUID shopId);
    List<Product> findByShopIdAndAvailable(UUID shopId, boolean available);
}

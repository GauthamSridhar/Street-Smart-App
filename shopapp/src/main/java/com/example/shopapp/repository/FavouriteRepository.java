// repository/FavoriteRepository.java
package com.example.shopapp.repository;

import com.example.shopapp.model.Favourite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavouriteRepository extends JpaRepository<Favourite, UUID> {
    List<Favourite> findByUserId(UUID userId);
    Optional<Favourite> findByUserIdAndShopId(UUID userId, UUID shopId);
    boolean existsByUserIdAndShopId(UUID userId, UUID shopId);
}

// repository/FavoriteRepository.java
package com.example.shopapp.repository;

import com.example.shopapp.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavouriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByUserId(UUID userId);
    Optional<Favorite> findByUserIdAndShopId(UUID userId, UUID shopId);
    boolean existsByUserIdAndShopId(UUID userId, UUID shopId);

    void deleteByUserIdAndShopId(UUID userId, UUID shopId);
}

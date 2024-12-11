// repository/RatingRepository.java
package com.shopapp.RatingService.repository;

import com.shopapp.RatingService.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, UUID> {
    List<Rating> findByShopId(UUID shopId);

    @Query(value = "SELECT AVG(r.rating) FROM ratings r WHERE r.shop_id = :shopId", nativeQuery = true)
    Float calculateAverageRating(UUID shopId);
    @Query(value = "SELECT COUNT(r.id) FROM ratings r WHERE r.shop_id = :shopId", nativeQuery = true)
    Long countByShopId(UUID shopId);
}

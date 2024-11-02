// repository/RatingRepository.java
package com.example.shopapp.repository;

import com.example.shopapp.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, UUID> {
    List<Rating> findByShopId(UUID shopId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.shop.id = ?1")
    Float calculateAverageRating(UUID shopId);
}

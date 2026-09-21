package com.shopapp.RatingService.repository;

import com.shopapp.RatingService.model.Rating;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

public interface RatingRepository extends JpaRepository<Rating, UUID> {
  List<Rating> findByShopId(UUID shopId, Pageable page);

  Long countByShopId(UUID shopId);

  boolean existsByUserIdAndShopId(UUID userId, UUID shopId);

  @Query("select coalesce(avg(r.rating),0) from Rating r where r.shopId = :shopId")
  double averageByShopId(UUID shopId);
}

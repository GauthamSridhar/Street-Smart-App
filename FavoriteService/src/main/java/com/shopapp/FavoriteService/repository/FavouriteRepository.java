package com.shopapp.FavoriteService.repository;

import com.shopapp.FavoriteService.model.Favorite;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavouriteRepository extends JpaRepository<Favorite, UUID> {
  boolean existsByUserIdAndShopId(UUID userId, UUID shopId);

  Optional<Favorite> findByUserIdAndShopId(UUID userId, UUID shopId);

  void deleteByUserIdAndShopId(UUID userId, UUID shopId);

  List<Favorite> findByUserId(UUID userId, Pageable page);

  long countByUserId(UUID userId);
}

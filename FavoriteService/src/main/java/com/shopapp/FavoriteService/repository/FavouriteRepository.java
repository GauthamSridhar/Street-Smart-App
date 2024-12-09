package com.shopapp.FavoriteService.repository;

import com.shopapp.FavoriteService.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FavouriteRepository extends JpaRepository<Favorite, UUID> {

    boolean existsByUserIdAndShopId(UUID userId, UUID shopId);
    void deleteByUserIdAndShopId(UUID userId, UUID shopId);

    List<Favorite> findByUserId(UUID userId);
}

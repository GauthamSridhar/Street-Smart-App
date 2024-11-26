package com.shopapp.ShopService.repository;

import com.shopapp.ShopService.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {
    List<Image> findByShopId(UUID shopId);
}

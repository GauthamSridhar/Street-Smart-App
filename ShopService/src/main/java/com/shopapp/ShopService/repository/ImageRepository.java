package com.shopapp.ShopService.repository;

import com.shopapp.ShopService.model.Image;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, UUID> {
  List<Image> findByShopId(UUID shopId);

  long countByShopId(UUID shopId);
}

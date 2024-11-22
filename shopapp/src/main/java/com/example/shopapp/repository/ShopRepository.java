// repository/ShopRepository.java
package com.example.shopapp.repository;

import com.example.shopapp.model.Shop;
import com.example.shopapp.model.ShopStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShopRepository extends JpaRepository<Shop, UUID> {
    List<Shop> findByStatus(ShopStatus status);
    List<Shop> findByOwnerId(UUID ownerId);

    @Query(value = "SELECT s.* FROM shops s " +
            "WHERE ST_DWithin(ST_MakePoint(s.longitude, s.latitude)::geography, " +
            "ST_MakePoint(?1, ?2)::geography, ?3)", nativeQuery = true)
    List<Shop> findNearbyShops(Double longitude, Double latitude, Double radiusInMeters);


}

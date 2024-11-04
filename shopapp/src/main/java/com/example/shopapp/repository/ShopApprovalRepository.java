// repository/ShopApprovalRepository.java
package com.example.shopapp.repository;

import com.example.shopapp.model.ShopApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShopApprovalRepository extends JpaRepository<ShopApproval, UUID> {
    Optional<ShopApproval> findByShopId(UUID shopId);

    List<ShopApproval> findByApprovalStatus(String pending);
}

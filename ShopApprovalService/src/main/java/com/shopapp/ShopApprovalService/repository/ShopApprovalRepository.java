package com.shopapp.ShopApprovalService.repository;

import com.shopapp.ShopApprovalService.model.ShopApproval;
import com.shopapp.ShopApprovalService.model.ShopStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShopApprovalRepository extends JpaRepository<ShopApproval, UUID> {
    Optional<ShopApproval> findByShopId(UUID shopId);

    List<ShopApproval> findByApprovalStatus(ShopStatus approvalStatus);
    Long countByApprovalStatus(ShopStatus shopStatus);
}

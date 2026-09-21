package com.shopapp.ShopApprovalService.repository;

import com.shopapp.ShopApprovalService.model.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

public interface ShopApprovalRepository extends JpaRepository<ShopApproval, UUID> {
  Optional<ShopApproval> findByShopId(UUID shopId);

  long countBySynchronizedWithShopFalse();

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from ShopApproval a where a.shopId = :shopId")
  Optional<ShopApproval> locked(UUID shopId);

  List<ShopApproval> findByApprovalStatus(ShopStatus status, Pageable page);

  Long countByApprovalStatus(ShopStatus status);

  List<ShopApproval> findTop50BySynchronizedWithShopFalseOrderById();
}

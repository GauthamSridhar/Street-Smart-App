package com.shopapp.ShopService.repository;

import com.shopapp.ShopService.model.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ShopRepository extends JpaRepository<Shop, UUID> {
  Shop findByOwnerId(UUID ownerId);

  boolean existsByOwnerId(UUID ownerId);

  long countByApprovalRequestedFalse();

  List<Shop> findTop50ByApprovalRequestedFalseOrderById();

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Shop s where s.id = :id")
  Optional<Shop> locked(UUID id);

  @Query(
      "select s from Shop s where (:all = true or s.status in :statuses) and (:owner is null or s.ownerId = :owner) and (:category = '' or lower(s.category) = lower(:category)) and (lower(s.name) like lower(concat('%',:q,'%')) or lower(s.description) like lower(concat('%',:q,'%')))")
  Page<Shop> search(
      boolean all,
      Collection<ShopStatus> statuses,
      UUID owner,
      String category,
      String q,
      Pageable page);
}

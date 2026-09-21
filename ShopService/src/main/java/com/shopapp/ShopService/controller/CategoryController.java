package com.shopapp.ShopService.controller;

import com.shopapp.ShopService.model.ShopStatus;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CategoryController {
  private final EntityManager entities;

  @GetMapping("/api/products/categories")
  @Transactional(readOnly = true)
  public List<String> categories() {
    return entities
        .createQuery(
            "select distinct lower(s.category) from Shop s where s.status in :statuses order by lower(s.category)",
            String.class)
        .setParameter(
            "statuses", List.of(ShopStatus.APPROVED, ShopStatus.ACTIVE, ShopStatus.INACTIVE))
        .setMaxResults(200)
        .getResultList();
  }
}

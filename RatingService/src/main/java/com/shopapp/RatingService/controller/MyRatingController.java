package com.shopapp.RatingService.controller;

import com.shopapp.RatingService.dto.rating.response.RatingResponseDTO;
import com.shopapp.RatingService.mapper.RatingMapper;
import com.shopapp.RatingService.model.Rating;
import com.shopapp.common.ApiException;
import com.shopapp.common.Caller;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/** Owned review lookup must not depend on the current page of public reviews. */
@RestController
@RequiredArgsConstructor
public class MyRatingController {
  private final EntityManager entityManager;
  private final RatingMapper mapper;

  @GetMapping("/api/ratings/mine/{shopId}")
  @Transactional(readOnly = true)
  public RatingResponseDTO mine(@PathVariable UUID shopId) {
    Caller.role("USER");
    return entityManager
        .createQuery(
            "select r from Rating r where r.userId = :userId and r.shopId = :shopId", Rating.class)
        .setParameter("userId", Caller.id())
        .setParameter("shopId", shopId)
        .getResultStream()
        .findFirst()
        .map(mapper::toDTO)
        .orElseThrow(() -> ApiException.notFound("Rating"));
  }
}

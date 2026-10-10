package com.shopapp.RatingService.repository;

import com.shopapp.RatingService.model.ReviewReport;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, UUID> {
  boolean existsByRatingIdAndReporterId(UUID ratingId, UUID reporterId);

  org.springframework.data.domain.Page<ReviewReport> findByStatus(
      String status, org.springframework.data.domain.Pageable pageable);
}

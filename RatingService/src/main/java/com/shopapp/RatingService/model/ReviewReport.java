package com.shopapp.RatingService.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(
    name = "review_reports",
    uniqueConstraints = @UniqueConstraint(columnNames = {"rating_id", "reporter_id"}))
public class ReviewReport {
  @Id private UUID id;

  @Column(nullable = false)
  private UUID ratingId;

  @Column(nullable = false)
  private UUID reporterId;

  @Column(nullable = false, length = 500)
  private String reason;

  @Column(nullable = false, length = 16)
  private String status;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  private LocalDateTime resolvedAt;
  private UUID resolvedBy;

  @Column(nullable = false, length = 2000)
  private String reviewSnapshot;

  @Version private long version;
}

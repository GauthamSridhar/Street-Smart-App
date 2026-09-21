package com.shopapp.RatingService.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(
    name = "ratings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "shop_id"}))
public class Rating {
  @Id @GeneratedValue private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "shop_id", nullable = false)
  private UUID shopId;

  @Column(nullable = false)
  private Integer rating;

  @Column(length = 2000)
  private String review;

  @Version private long version;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}

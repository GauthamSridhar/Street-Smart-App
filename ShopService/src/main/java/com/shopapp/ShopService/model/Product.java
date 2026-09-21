package com.shopapp.ShopService.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "products")
public class Product {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  private String name;

  private boolean available;

  @Column(length = 1000)
  private String description;

  @Column(precision = 12, scale = 2)
  private java.math.BigDecimal price;

  @Column(length = 3)
  private String currency;

  private java.time.Instant updatedAt;

  @PrePersist
  @PreUpdate
  void recordUpdate() {
    updatedAt = java.time.Instant.now();
  }

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "shop_id", nullable = false)
  private Shop shop;
}

package com.shopapp.FavoriteService.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(
    name = "favorites",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "shop_id"}))
public class Favorite {
  @Id @GeneratedValue private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "shop_id", nullable = false)
  private UUID shopId;

  @Column(nullable = false, length = 100)
  private String shopName;
}

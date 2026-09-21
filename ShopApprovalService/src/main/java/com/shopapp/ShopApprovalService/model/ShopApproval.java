package com.shopapp.ShopApprovalService.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "shop_approvals")
public class ShopApproval {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private UUID shopId;

  private long revision;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ShopStatus approvalStatus;

  @Column(length = 1000)
  private String reason;

  @Column(nullable = false)
  private Boolean approved = false;

  private UUID decidedBy;
  private LocalDateTime decidedAt;

  @Column(nullable = false)
  private boolean synchronizedWithShop = true;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  @Version private long version;

  @PrePersist
  public void create() {
    createdAt = LocalDateTime.now();
    updatedAt = createdAt;
  }

  @PreUpdate
  public void update() {
    updatedAt = LocalDateTime.now();
  }
}

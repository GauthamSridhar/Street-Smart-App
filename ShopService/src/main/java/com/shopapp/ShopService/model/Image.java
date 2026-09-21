package com.shopapp.ShopService.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "images")
public class Image {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Transient private byte[] imageData;

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String fileType;

  @Column(nullable = false)
  private long fileSizeInBytes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shop_id", nullable = false)
  private Shop shop;
}

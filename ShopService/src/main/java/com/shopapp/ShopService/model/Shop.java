package com.shopapp.ShopService.model;

import jakarta.persistence.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "shops")
public class Shop {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 1000)
  private String description;

  @Column(nullable = false, length = 60)
  private String category;

  @Column(length = 255)
  private String openingHours;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private Double latitude;

  @Column(nullable = false)
  private Double longitude;

  @Column(nullable = false, unique = true)
  private UUID ownerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ShopStatus status;

  @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Product> products = new ArrayList<>();

  @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Image> images = new ArrayList<>();

  @Column(nullable = false)
  private boolean approvalRequested;

  private long approvalRevision;

  @Version private long version;
}

package com.shopapp.ShopService.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/** Binary content is separate so catalogue queries never hydrate upload bytes. */
@Getter
@Setter
@Entity
@Table(name = "image_contents")
public class ImageContent {
  @Id private UUID imageId;

  @Column(nullable = false)
  private byte[] data;
}

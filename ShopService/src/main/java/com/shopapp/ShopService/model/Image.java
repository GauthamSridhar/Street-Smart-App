package com.shopapp.ShopService.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String url;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop; // Reference to the Shop
}

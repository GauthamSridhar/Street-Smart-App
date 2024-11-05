package com.example.shopapp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "shops")
public class Shop {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String address;

    private Double latitude;
    private Double longitude;

    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner; // Shopkeeper is a User

    @Enumerated(EnumType.STRING)
    private ShopStatus status;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();


    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Rating> ratings = new ArrayList<>();

    @OneToOne(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private ShopApproval shopApproval;

    // Constructors
    public Shop() {
        this.products = new ArrayList<>();
        this.images = new ArrayList<>();
        this.ratings = new ArrayList<>();
    }
}

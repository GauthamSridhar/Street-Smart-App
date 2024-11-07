package com.example.shopapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Data
@Entity
@Table(name = "shop_images")
public class Image {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String imageUrl;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;
}

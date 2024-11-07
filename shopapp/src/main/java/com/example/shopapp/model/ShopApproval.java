package com.example.shopapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "shop_approvals", uniqueConstraints = {
        @UniqueConstraint(columnNames = "shop_id")
})
public class ShopApproval {
    @Id
    @GeneratedValue
    private UUID id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "shop_id", nullable = false, unique = true)
    private Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShopStatus approvalStatus;

    private String reason;

    @Column(nullable = false)
    private Boolean approved;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.approved = false; // Default value for approved
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

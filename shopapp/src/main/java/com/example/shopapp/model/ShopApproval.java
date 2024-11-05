package com.example.shopapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Entity
@Table(name = "shop_approvals")
public class ShopApproval {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonIgnore
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false) // Make admin_id not nullable
    @JsonIgnore
    private User admin; // Admin must be assigned for approval

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

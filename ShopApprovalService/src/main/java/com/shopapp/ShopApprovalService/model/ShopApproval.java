package com.shopapp.ShopApprovalService.model;

import jakarta.persistence.*;
import lombok.Data;

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

    @Column(name = "shop_id", nullable = false, unique = true)
    private UUID shopId;

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
        this.approved = false; // Default value
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

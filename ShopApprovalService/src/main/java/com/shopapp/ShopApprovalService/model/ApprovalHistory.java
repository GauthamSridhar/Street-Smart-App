package com.shopapp.ShopApprovalService.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "approval_history")
@Getter
@Setter
public class ApprovalHistory {
  @Id private UUID id;
  private UUID shopId;
  private long revision;
  private String status;
  private String reason;
  private UUID decidedBy;
  private LocalDateTime decidedAt;

  public static ApprovalHistory from(ShopApproval a) {
    var h = new ApprovalHistory();
    h.id = UUID.randomUUID();
    h.shopId = a.getShopId();
    h.revision = a.getRevision();
    h.status = a.getApprovalStatus().name();
    h.reason = a.getReason();
    h.decidedBy = a.getDecidedBy();
    h.decidedAt = a.getDecidedAt();
    return h;
  }
}

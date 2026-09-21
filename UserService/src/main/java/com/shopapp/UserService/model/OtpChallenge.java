package com.shopapp.UserService.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "otp_challenges")
public class OtpChallenge {
  @Id
  @Column(length = 16)
  private String phone;

  @Column(nullable = false)
  private String codeHash;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private Instant sentAt;

  private int attempts;
  private String proofHash;
  private Instant proofExpiresAt;
}

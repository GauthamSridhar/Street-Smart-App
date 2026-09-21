package com.shopapp.UserService.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
public class UserSession {
  @Id private UUID id;
  private UUID userId;
  private Instant expiresAt;
  private Instant createdAt;
}
